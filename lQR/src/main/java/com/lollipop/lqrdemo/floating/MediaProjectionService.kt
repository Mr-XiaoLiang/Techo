package com.lollipop.lqrdemo.floating

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log

class MediaProjectionService : Service() {

    companion object {

        private const val PARAMS_RESULT_KEY = "ResultKey"
        private const val ACTION_SCREENSHOT = "ActionScreenshot"

        fun start(context: Context, resultKey: Int) {
            context.startForegroundService(
                Intent(context, MediaProjectionService::class.java).apply {
                    putExtra(PARAMS_RESULT_KEY, resultKey)
                }
            )
        }

        private fun createScreenshotDelegate(
            manager: MediaProjectionManager?,
            mpResult: MediaProjectionHelper.MPResult
        ): ScreenshotDelegate? {
            manager ?: return null
            val mp = mpResult.getMediaProjection(manager) ?: return null
            return ScreenshotDelegate(mp)
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        tryDo("onStartCommand") {
            val key = intent?.getIntExtra(PARAMS_RESULT_KEY, 0) ?: 0
            val mpResult = MediaProjectionHelper.findResult(key)
            if (mpResult != null) {
                screenshotDelegate?.release()
                screenshotDelegate = createScreenshotDelegate(
                    mediaProjectionManager, mpResult
                )
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
        // TODO
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaProjectionHelper.serviceState = MediaProjectionHelper.ServiceState.STOPPED
    }

    private class ScreenshotDelegate(
        val mediaProjection: MediaProjection
    ) {

        init {
            createImageReaderVirtualDisplay()
        }

        private fun createImageReaderVirtualDisplay() {
            // TODO
        }

        fun release() {
            // TODO
        }

    }

}