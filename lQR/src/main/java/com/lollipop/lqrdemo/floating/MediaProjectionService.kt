package com.lollipop.lqrdemo.floating

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
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
import com.lollipop.base.util.onUI
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.floating.view.FloatingActionInvokeCallback
import com.lollipop.lqrdemo.floating.view.FloatingViewConfig
import com.lollipop.qr.comm.ImageToBitmap
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class MediaProjectionService : Service() {

    companion object {

        private const val PARAMS_RESULT_KEY = "ResultKey"

        private const val REQUEST_CODE_SERVICE_ACTION = 666

        fun start(context: Context, resultKey: Int) {
            context.startService(
                Intent(context, MediaProjectionService::class.java).apply {
                    putExtra(PARAMS_RESULT_KEY, resultKey)
                }
            )
        }

        fun sendScreenshotBroadcast(context: Context) {
            ServiceActionBroadcast.sendScreenshot(context)
        }

    }

    private var screenshotDelegate: ScreenshotDelegate? = null
    private val mediaProjectionManager by lazy {
        MediaProjectionHelper.getManager(this)
    }

    private val floatingViewDelegate = FloatingViewDelegate()

    private val serviceReceiver by lazy {
        ServiceActionBroadcast.receiver()
            .stop {
                NotificationHelper.removeForegroundNotification(this)
                stopSelf()
            }
            .showActionButton {
                floatingViewDelegate.show(it)
            }
            .hideActionButton {
                floatingViewDelegate.hide()
            }
            .build()
    }

    private fun tryDo(name: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            Log.e("MediaProjectionService", "${name}.ERROR", e)
        }
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
        floatingViewDelegate.closeAvoidance()
        when (result) {
            is ScreenshotResult.Success -> {
                val scanResult = FloatingScanHelper.startScanResult(this, result.url)
                Log.d("MediaProjectionService", "startScanResult: $scanResult")
            }

            is ScreenshotResult.Failure -> {
                Toast.makeText(
                    this,
                    getString(R.string.toast_screenshot_result_failure, result.error.message),
                    Toast.LENGTH_SHORT
                ).show()
            }

            ScreenshotResult.NoAvailable -> {
                Toast.makeText(
                    this,
                    R.string.toast_screenshot_result_no_available,
                    Toast.LENGTH_SHORT
                ).show()
            }

            ScreenshotResult.UnknownError -> {
                Toast.makeText(
                    this,
                    R.string.toast_screenshot_result_unknown_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendForegroundNotification() {
        NotificationHelper.sendForegroundNotification(this) { builder ->
            addAction(
                builder,
                com.lollipop.lqrdemo.R.drawable.ic_lqr_24dp,
                com.lollipop.lqrdemo.R.string.notification_action_stop_floating,
                ServiceActionBroadcast.ACTION_STOP
            )
            if (floatingViewDelegate.isVisible) {
                // 可见的时候就隐藏
                addAction(
                    builder,
                    0,
                    com.lollipop.lqrdemo.R.string.notification_action_hide_floating,
                    ServiceActionBroadcast.ACTION_HIDE_ACTION_BUTTON
                )
            } else {
                // 隐藏的时候就显示
                addAction(
                    builder,
                    0,
                    com.lollipop.lqrdemo.R.string.notification_action_show_floating,
                    ServiceActionBroadcast.ACTION_SHOW_ACTION_BUTTON
                )
            }
        }
    }

    private fun addAction(builder: Notification.Builder, icon: Int, label: Int, action: String) {
        val context = this@MediaProjectionService
        builder.addAction(
            Notification.Action.Builder(
                if (icon == 0) {
                    null
                } else {
                    Icon.createWithResource(context, icon)
                },
                getString(label),
                PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE_SERVICE_ACTION,
                    ServiceActionBroadcast.getIntent(context, action),
                    PendingIntent.FLAG_IMMUTABLE
                )
            ).build()
        )
    }

    override fun onCreate() {
        super.onCreate()
        serviceReceiver.attach(this)
        val fabSizeDp = FloatingActionButton.getFabSizeDp()
        floatingViewDelegate.attach(
            FloatingActionButton.Factory,
            config = FloatingViewConfig(
                widthDp = fabSizeDp,
                heightDp = fabSizeDp,
            )
        )
        floatingViewDelegate.viewInvokeCallback = FloatingActionInvokeCallback { c ->
            floatingViewDelegate.enableAvoidance()
            ServiceActionBroadcast.sendScreenshot(c)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sendForegroundNotification()
        tryDo("onStartCommand") {
            val key = intent?.getIntExtra(PARAMS_RESULT_KEY, 0) ?: 0
            val mpResult = MediaProjectionHelper.findResult(key)
            if (mpResult != null) {
                screenshotDelegate?.let {
                    it.receiver.detach(this)
                    it.release()
                }
                screenshotDelegate = createScreenshotDelegate(mpResult)
                screenshotDelegate?.receiver?.attach(this)
            }
        }
        if (screenshotDelegate != null) {
            MediaProjectionHelper.serviceState = MediaProjectionHelper.ServiceState.RUNNING
            floatingViewDelegate.show(this)
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
        serviceReceiver.detach(this)
        floatingViewDelegate.detach()
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

        val receiver = ServiceActionBroadcast.receiver().screenshot { notifyScreenshot() }.build()

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
                val file = saveBitmap(bitmap)
                return ScreenshotResult.Success(file.path)
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
            val timestamp = System.currentTimeMillis().toString(16)
            val file = File(context.cacheDir, "screenshot${timestamp}.png")
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


}