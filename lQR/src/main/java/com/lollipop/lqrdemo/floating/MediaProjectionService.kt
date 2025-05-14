package com.lollipop.lqrdemo.floating

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.lollipop.base.util.onUI
import com.lollipop.lqrdemo.R
import com.lollipop.qr.comm.ImageToBitmap
import java.util.concurrent.Executors

class MediaProjectionService : Service() {

    companion object {

        private const val PARAMS_RESULT_KEY = "ResultKey"
        private const val ACTION_SCREENSHOT = "ActionScreenshot"

        private const val CHANNEL_ID_MEDIA_PROJECTION = "CHANNEL_ID_MEDIA_PROJECTION"
        private const val NOTIFICATION_ID_MEDIA_PROJECTION = 2333

        fun start(context: Context, resultKey: Int) {
            context.startForegroundService(
                Intent(context, MediaProjectionService::class.java).apply {
                    putExtra(PARAMS_RESULT_KEY, resultKey)
                }
            )
        }

        fun sendScreenshotBroadcast(context: Context) {
            context.sendBroadcast(Intent(ACTION_SCREENSHOT).setPackage(context.packageName))
        }

    }

    private var screenshotDelegate: ScreenshotDelegate? = null
    private val mediaProjectionManager by lazy {
        MediaProjectionHelper.getManager(this)
    }

    private fun tryDo(name: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            Log.e("MediaProjectionService", "${name}.ERROR", e)
        }
    }

    private fun registerScreenshotReceiver(receiver: BroadcastReceiver) {
        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(ACTION_SCREENSHOT),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun createScreenshotDelegate(
        mpResult: MediaProjectionHelper.MPResult
    ): ScreenshotDelegate? {
        val manager = mediaProjectionManager ?: return null
        val mp = mpResult.getMediaProjection(manager) ?: return null
        return ScreenshotDelegate(this, mp, ::onScreenshot)
    }

    private fun onScreenshot(result: ScreenshotResult) {
        // TODO
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        tryDo("onStartCommand") {
            val key = intent?.getIntExtra(PARAMS_RESULT_KEY, 0) ?: 0
            val mpResult = MediaProjectionHelper.findResult(key)
            if (mpResult != null) {
                screenshotDelegate?.let {
                    unregisterReceiver(it.receiver)
                    it.release()
                }
                screenshotDelegate = createScreenshotDelegate(
                    mpResult
                )
                screenshotDelegate?.let {
                    registerScreenshotReceiver(it.receiver)
                }
            }
        }
        if (screenshotDelegate != null) {
            sendForegroundNotification()
            MediaProjectionHelper.serviceState = MediaProjectionHelper.ServiceState.RUNNING
        } else {
            MediaProjectionHelper.serviceState = MediaProjectionHelper.ServiceState.STOPPED
            stopSelf()
        }
        return START_STICKY_COMPATIBILITY
    }

    private fun sendForegroundNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID_MEDIA_PROJECTION,
            getString(R.string.notification_channel_scan),
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = Notification.Builder(this, CHANNEL_ID_MEDIA_PROJECTION)
            .setSmallIcon(R.drawable.ic_lqr_24dp)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_content_scan))

        val notification = notificationBuilder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID_MEDIA_PROJECTION,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID_MEDIA_PROJECTION, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaProjectionHelper.serviceState = MediaProjectionHelper.ServiceState.STOPPED
    }

    private class ScreenshotDelegate(
        private val context: Context,
        private val mediaProjection: MediaProjection,
        private val onScreenshotResult: (ScreenshotResult) -> Unit
    ) : MediaProjection.Callback() {

        private var imageReader: ImageReader? = null
        private var virtualDisplayImageReader: VirtualDisplay? = null

        var isAvailable = false
            private set

        val receiver = ScreenshotReceiver(::notifyScreenshot)

        private val executor by lazy {
            Executors.newSingleThreadExecutor()
        }

        init {
            createImageReaderVirtualDisplay()
        }

        private fun notifyScreenshot() {
            val reader = imageReader
            if (!isAvailable || reader == null) {
                onScreenshotResult(ScreenshotResult.NoAvailable)
                return
            }
            executor.execute {
                try {
                    val result = screenshot(reader)
                    onUI {
                        onScreenshotResult(result)
                    }
                } catch (e: Throwable) {
                    onUI {
                        onScreenshotResult(ScreenshotResult.Failure(e))
                    }
                }
            }
        }

        private fun screenshot(reader: ImageReader): ScreenshotResult {
            val latestImage = reader.acquireLatestImage()
            if (latestImage == null) {
                return ScreenshotResult.NoAvailable
            }
            val result = ImageToBitmap.parse(latestImage)
            if (result.isSuccess) {
                val bitmap = result.getOrNull()
                // 保存
                TODO("需要保存")
            } else {
                val error = result.exceptionOrNull()
                if (error != null) {
                    return ScreenshotResult.Failure(error)
                } else {
                    return ScreenshotResult.UnknownError
                }
            }
            // TODO
        }

        private fun getDisplayMetrics(): DisplayMetrics? {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (windowManager == null) {
                return null
            }
            val displayMetrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = windowManager.currentWindowMetrics
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    displayMetrics.densityDpi = windowMetrics.density.toInt()
                } else {
                    context.display.getRealMetrics(displayMetrics)
                }
                displayMetrics.widthPixels = windowMetrics.bounds.width()
                displayMetrics.heightPixels = windowMetrics.bounds.height()
            } else {
                windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            }
            return displayMetrics
        }

        private fun createImageReaderVirtualDisplay() {
            val displayMetrics = getDisplayMetrics() ?: return
            val reader = ImageReader.newInstance(
                displayMetrics.widthPixels, displayMetrics.heightPixels, PixelFormat.RGBA_8888, 1
            )
            imageReader = reader
            reader.setOnImageAvailableListener(
                ImageReader.OnImageAvailableListener { reader: ImageReader? ->
                    isAvailable = true
                },
                null
            )
            mediaProjection.registerCallback(this, null)
            virtualDisplayImageReader =
                mediaProjection.createVirtualDisplay(
                    "ImageReader",
                    displayMetrics.widthPixels,
                    displayMetrics.heightPixels,
                    displayMetrics.densityDpi,
                    Display.FLAG_ROUND,
                    reader.surface,
                    null,
                    null
                )
        }

        fun release() {
            executor.shutdown()
            mediaProjection.unregisterCallback(this)
            virtualDisplayImageReader?.release()
            virtualDisplayImageReader = null
            // TODO
        }

        override fun onCapturedContentResize(width: Int, height: Int) {
            super.onCapturedContentResize(width, height)
            // TODO
        }

        override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
            super.onCapturedContentVisibilityChanged(isVisible)
            // TODO
        }

        override fun onStop() {
            super.onStop()
            // TODO
        }

    }

    private class ScreenshotReceiver(
        private val notifyScreenshot: () -> Unit
    ) : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            if (intent.action == ACTION_SCREENSHOT) {
                notifyScreenshot()
            }
        }

    }

}