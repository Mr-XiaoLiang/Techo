package com.lollipop.lqrdemo.floating

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

object MediaProjectionHelper {

    const val KEY_NONE = 0

    private val mpResultCache = mutableMapOf<Int, MPResult>()
    private var cacheKeyIndex = KEY_NONE + 1
    var serviceState = ServiceState.IDLE

    val isServiceRunning: Boolean
        get() {
            return serviceState == ServiceState.RUNNING
        }

    private fun putResult(result: MPResult): Int {
        cacheKeyIndex++
        mpResultCache[cacheKeyIndex] = result
        return cacheKeyIndex
    }

    fun getManager(context: Context): MediaProjectionManager? {
        return context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
    }

    fun findResult(key: Int): MPResult? {
        return mpResultCache.remove(key)
    }

    fun register(
        activity: AppCompatActivity,
        resultCallback: (LaunchResult) -> Unit
    ): MPLauncher {
        return MPLauncher(activity, resultCallback)
    }

    class MPLauncher(
        private val activity: AppCompatActivity,
        private val resultCallback: (LaunchResult) -> Unit
    ) : LifecycleEventObserver {

        private var launcher: ActivityResultLauncher<Intent>? = null

        val manager by lazy {
            getManager(activity)
        }

        init {
            activity.lifecycle.addObserver(this)
        }

        private fun onCreate() {
            launcher = activity.registerForActivityResult(
                StartActivityForResult(), ::onResult
            )
        }

        fun launch() {
            val mpManager = manager
            val l = launcher
            if (mpManager == null || l == null) {
                resultCallback(LaunchResult.Failed)
                return
            }
            l.launch(mpManager.createScreenCaptureIntent())
        }

        private fun onResult(result: ActivityResult) {
            try {
                val resultCode = result.resultCode
                val data = result.data
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        val key = putResult(MPResult(resultCode, data))
                        MediaProjectionService.start(activity, key)
                        resultCallback(LaunchResult.Success)
                    } else {
                        resultCallback(LaunchResult.Failed)
                    }
                } else {
                    resultCallback(LaunchResult.Cancel)
                }
            } catch (e: Throwable) {
                Log.e("MediaProjectionHelper.MPLauncher", "onResult.ERROR", e)
                resultCallback(LaunchResult.Failed)
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

    class MPResult(
        val resultCode: Int,
        val data: Intent
    ) {

        fun getMediaProjection(context: Context): MediaProjection? {
            val manager = getManager(context) ?: return null
            return getMediaProjection(manager)
        }

        fun getMediaProjection(manager: MediaProjectionManager): MediaProjection? {
            return manager.getMediaProjection(resultCode, data)
        }

    }

    sealed class LaunchResult {
        object Success : LaunchResult()
        object Failed : LaunchResult()
        object Cancel : LaunchResult()
    }

    enum class ServiceState {
        IDLE,
        RUNNING,
        ERROR,
        STOPPED
    }

}