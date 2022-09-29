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