package com.lollipop.lqrdemo.floating

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.drawable.Icon
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.lollipop.base.util.delay
import com.lollipop.base.util.onUI
import com.lollipop.qr.comm.ImageToBitmap
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class MediaProjectionService : Service() {

    companion object {

        private const val PARAMS_RESULT_KEY = "ResultKey"
        private const val ACTION_SCREENSHOT = "lollipop.lqr.ActionScreenshot"
        private const val ACTION_STOP = "lollipop.lqr.ActionStop"

        private const val REQUEST_CODE_STOP_SERVICE = 666

        fun start(context: Context, resultKey: Int) {
            context.startService(
                Intent(context, MediaProjectionService::class.java).apply {
                    putExtra(PARAMS_RESULT_KEY, resultKey)
                }
            )
        }

        fun sendScreenshotBroadcast(context: Context) {
            context.sendBroadcast(Intent(ACTION_SCREENSHOT).setPackage(context.packageName))
        }

        fun createStopBroadcast(context: Context): Intent {
            return Intent(ACTION_STOP).setPackage(context.packageName)
        }

    }

    private var screenshotDelegate: ScreenshotDelegate? = null
    private val mediaProjectionManager by lazy {
        MediaProjectionHelper.getManager(this)
    }

    private val stopServiceReceiver = StopReceiver {
        NotificationHelper.removeForegroundNotification(this)
        stopSelf()
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
        Log.d("MediaProjectionService", "onScreenshot: $result")
        when (result) {
            is ScreenshotResult.Success -> {
                val scanResult = FloatingScanHelper.startScanResult(this, result.url)
                Log.d("MediaProjectionService", "startScanResult: $scanResult")
            }

            is ScreenshotResult.Failure -> {
                // TODO()
                Toast.makeText(this, result.error.message, Toast.LENGTH_SHORT).show()
            }

            ScreenshotResult.NoAvailable -> {
                // TODO()
                Toast.makeText(this, "NoAvailable", Toast.LENGTH_SHORT).show()
            }

            ScreenshotResult.UnknownError -> {
                // TODO()
                Toast.makeText(this, "UnknownError", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendForegroundNotification() {
        NotificationHelper.sendForegroundNotification(this) {
            it.addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(
                        this,
                        com.lollipop.lqrdemo.R.drawable.ic_lqr_24dp
                    ),
                    getString(
                        com.lollipop.lqrdemo.R.string.notification_action_stop_floating
                    ),
                    PendingIntent.getBroadcast(
                        this, REQUEST_CODE_STOP_SERVICE,
                        createStopBroadcast(this),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                ).build()
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        ContextCompat.registerReceiver(
            this,
            stopServiceReceiver,
            IntentFilter(ACTION_STOP),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sendForegroundNotification()
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
            MediaProjectionHelper.serviceState = MediaProjectionHelper.ServiceState.RUNNING
            delay(15000) {
                Toast.makeText(this, "sendScreenshotBroadcast", Toast.LENGTH_SHORT).show()
                sendScreenshotBroadcast(this)
            }
        } else {
            MediaProjectionHelper.serviceState = MediaProjectionHelper.ServiceState.STOPPED
            stopSelf()
        }
        return START_STICKY_COMPATIBILITY
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
                if (bitmap == null) {
                    return ScreenshotResult.UnknownError
                }
                if (bitmap is Bitmap) {
                    val file = saveBitmap(bitmap)
                    return ScreenshotResult.Success(file.path)
                } else {
                    return ScreenshotResult.UnknownError
                }
            } else {
                val error = result.exceptionOrNull()
                return if (error != null) {
                    ScreenshotResult.Failure(error)
                } else {
                    ScreenshotResult.UnknownError
                }
            }
        }

        private fun saveBitmap(bitmap: Bitmap): File {
            val file = File(context.cacheDir, "screenshot.png")
            if (file.exists()) {
                file.delete()
            }
            val fos = FileOutputStream(file)
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            } catch (e: Throwable) {
                Log.e("Lollipop", "MediaProjectionService.ScreenshotDelegate.saveBitmap.ERROR", e)
            } finally {
                fos.close()
            }
            return file
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
        }

        override fun onCapturedContentResize(width: Int, height: Int) {
            super.onCapturedContentResize(width, height)
        }

        override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
            super.onCapturedContentVisibilityChanged(isVisible)
        }

        override fun onStop() {
            super.onStop()
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

    private class StopReceiver(
        private val notifyStop: () -> Unit
    ) : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            if (intent.action == ACTION_STOP) {
                notifyStop()
            }
        }

    }

}