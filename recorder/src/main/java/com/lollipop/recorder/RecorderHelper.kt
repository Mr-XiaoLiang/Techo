package com.lollipop.recorder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.app.ActivityCompat
import java.io.File

class RecorderHelper(
    private val context: Context
) {

    companion object {
        fun checkPermission(activity: Activity): Boolean {
            return ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }

        var globalCacheDir: File? = null
    }

    private var mediaRecorder: MediaRecorder? = null

    var currentCacheFile: File? = null
        private set

    private var recorderBuilder: RecorderBuilder? = null

    private val recorderCacheDir by lazy {
        File(globalCacheDir ?: context.cacheDir, "recorder")
    }

    private fun buildConfig(recorder: MediaRecorder) {
        val builder = recorderBuilder
        if (builder != null) {
            builder.setRecorderConfig(recorder)
            return
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD)
        recorder.setAudioChannels(2)
        val outFile = File(
            recorderCacheDir,
            java.lang.Long.toHexString(System.currentTimeMillis()) + ".aac"
        )
        recorder.setOutputFile(outFile.path)
    }

    /**
     * 通过getMaxAmplitude()方法获得最近的最大振幅，然后除以振幅的最大值32767
     * 得到振幅的百分比
     * @return 返回振幅的百分比[0.0, 1.0]
     */
    fun getAmplitude(): Float {
        try {
            val maxAmplitude = mediaRecorder?.maxAmplitude ?: return 0F
            return maxAmplitude / 32767F
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return 0F
    }

    private fun init() {
        if (mediaRecorder != null) {
            return
        }
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
        mediaRecorder = recorder
        buildConfig(recorder)
        recorder.prepare()
    }

    fun start() {
        init()
        mediaRecorder?.start()
    }

    fun resume() {
        mediaRecorder?.resume()
    }

    fun pause() {
        mediaRecorder?.pause()
    }

    fun stop() {
        mediaRecorder?.stop()
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    fun setRecorderBuilder(builder: RecorderBuilder?) {
        this.recorderBuilder = builder
    }

    fun interface RecorderBuilder {
        fun setRecorderConfig(recorder: MediaRecorder)
    }

}