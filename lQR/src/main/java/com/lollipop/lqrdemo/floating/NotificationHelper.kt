package com.lollipop.lqrdemo.floating

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lollipop.lqrdemo.R

object NotificationHelper {

    private const val CHANNEL_ID_MEDIA_PROJECTION = "CHANNEL_ID_MEDIA_PROJECTION"
    private const val CHANNEL_ID_SCAN_RESULT = "CHANNEL_ID_SCAN_RESULT"
    private const val NOTIFICATION_ID_MEDIA_PROJECTION = 2333

    private const val NOTIFICATION_ID_FULL_SCREEN = 996

    const val REQUEST_CODE_FULL_SCREEN = 666

    private fun createChannel(context: Context, manager: NotificationManager) {
        val channelList = mutableListOf<NotificationChannel>()
        Channel.entries.forEach { entity ->
            val channel = NotificationChannel(
                entity.id,
                context.getString(entity.displayName),
                entity.importance
            )
            channelList.add(channel)
        }
        manager.createNotificationChannels(channelList)
    }

    fun removeForegroundNotification(context: Context) {
        getNotificationManager(context)?.cancel(NOTIFICATION_ID_MEDIA_PROJECTION)
    }

    fun removeFullScreenNotification(context: Context) {
        getNotificationManager(context)?.cancel(NOTIFICATION_ID_FULL_SCREEN)
    }

    private fun getNotificationManager(context: Context): NotificationManager? {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager == null) {
            return null
        }
        createChannel(context, manager)
        return manager
    }

    fun startFullScreen(
        context: Context,
        channel: Channel,
        intent: Intent,
        contentText: Int
    ): FullScreenResult {

        try {

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val notificationManager =
                    getNotificationManager(context) ?: return FullScreenResult.ManagerNotFound

                val fullScreenPendingIntent = PendingIntent.getActivity(
                    context,
                    REQUEST_CODE_FULL_SCREEN,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                val notificationBuilder = NotificationCompat.Builder(context, channel.id)
                    .setSmallIcon(R.drawable.ic_lqr_24dp)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(contentText))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(Notification.CATEGORY_CALL)
                    .setOngoing(true)
                    .setFullScreenIntent(fullScreenPendingIntent, true)

                notificationManager.notify(NOTIFICATION_ID_FULL_SCREEN, notificationBuilder.build())

                try {
                    context.startActivity(intent)
                } catch (e: Throwable) {
                }
            } else {
                context.startActivity(intent)
            }
            return FullScreenResult.Success
        } catch (e: Throwable) {
            return FullScreenResult.Failed(e)
        }
    }


    fun sendForegroundNotification(
        context: Service,
        contentBuilder: (Notification.Builder) -> Unit
    ) {
        try {
            getNotificationManager(context) ?: return
            val notificationBuilder = Notification.Builder(context, Channel.SCAN_FLOATING.id)
                .setSmallIcon(R.drawable.ic_lqr_24dp)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_content_scan_floating))
            contentBuilder(notificationBuilder)
            val notification = notificationBuilder.build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.startForeground(
                    NOTIFICATION_ID_MEDIA_PROJECTION,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                )
            } else {
                context.startForeground(NOTIFICATION_ID_MEDIA_PROJECTION, notification)
            }
        } catch (e: Throwable) {
            Log.e("NotificationHelper", "sendForegroundNotification", e)
        }
    }

    enum class Channel(
        val id: String,
        val displayName: Int,
        val importance: Int,
    ) {
        SCAN_FLOATING(
            id = CHANNEL_ID_MEDIA_PROJECTION,
            displayName = R.string.notification_channel_scan_floating,
            importance = NotificationManager.IMPORTANCE_LOW,
        ),
        SCAN_RESULT(
            id = CHANNEL_ID_SCAN_RESULT,
            displayName = R.string.notification_channel_scan_result,
            importance = NotificationManager.IMPORTANCE_HIGH
        )
    }


    fun registerPermissionLauncher(
        activity: AppCompatActivity,
        resultCallback: (Boolean) -> Unit
    ): PermissionLauncher {
        return PermissionLauncher(activity, resultCallback)
    }

    class PermissionLauncher(
        private val activity: AppCompatActivity,
        private val resultCallback: (Boolean) -> Unit
    ) : LifecycleEventObserver {

        private var launcher: ActivityResultLauncher<String>? = null

        var hasPermission = false
            private set

        init {
            activity.lifecycle.addObserver(this)
        }

        private fun onCreate() {
            hasPermission = checkPermission() ?: false
            launcher = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission(),
                ::onResult
            )
        }

        private fun checkPermission(): Boolean? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notificationManager = activity.getSystemService(NotificationManager::class.java)
                if (notificationManager == null) {
                    return null
                }
                return notificationManager.areNotificationsEnabled()
            }
            return true
        }

        fun launch() {
            val l = launcher
            if (l == null) {
                onResult(false)
                return
            }
            val permission = checkPermission()
            if (permission == null) {
                onResult(false)
                return
            }
            if (permission) {
                onResult(true)
                return
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                l.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onResult(true)
            }
        }

        private fun onResult(result: Boolean) {
            hasPermission = result
            try {
                resultCallback(result)
            } catch (e: Throwable) {
                Log.e("NotificationHelper.PermissionLauncher", "onResult.ERROR", e)
            }
        }

        override fun onStateChanged(
            source: LifecycleOwner,
            event: Lifecycle.Event
        ) {
            if (event == Lifecycle.Event.ON_CREATE) {
                onCreate()
            }
        }

    }

    sealed class FullScreenResult {
        object Success : FullScreenResult()
        object ManagerNotFound : FullScreenResult()
        class Failed(val error: Throwable) : FullScreenResult()
    }

}