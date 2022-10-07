package com.lollipop.recorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.*

class RecorderHelper(
    private val context: Context
) {

    companion object {
        fun checkPermission(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }

        var globalCacheDir: File? = null

        private fun copy(fromFile: File, toFile: File): Boolean {
            if (!fromFile.exists()) {
                return false
            }
            if (!fromFile.isFile) {
                return false
            }
            if (toFile.exists()) {
                toFile.delete()
            }
            try {
                toFile.parentFile?.mkdirs()
            } catch (e: Throwable) {
                e.printStackTrace()
                return false
            }
            var inputSteam: InputStream? = null
            var outputSteam: OutputStream? = null
            try {
                val input = BufferedInputStream(FileInputStream(fromFile))
                inputSteam = input
                val output = BufferedOutputStream(FileOutputStream(toFile))
                outputSteam = output

                val buffer = ByteArray(1024 * 4)
                do {
                    val read = input.read(buffer)
                    if (read < 0) {
                        break
                    }
                    output.write(buffer, 0, read)
                } while (true)
                output.flush()
                return true
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                try {
                    outputSteam?.close()
                } catch (_: Throwable) {
                }
                try {
                    inputSteam?.close()
                } catch (_: Throwable) {
                }
            }
            return false
        }
    }

    private var mediaRecorder: MediaRecorder? = null

    var currentCacheFile: File? = null
        private set

    private var recorderBuilder: RecorderBuilder? = null

    private val recorderCacheDir by lazy {
        File(globalCacheDir ?: context.cacheDir, "recorder")
    }

    private var isRecording: Boolean = false

    var suffix: String = ".AAC"
        private set

    var state = State.INITIALIZED
        private set

    private var duration = 0L

    private var lastTime = 0L

    private var listenerListener = ArrayList<OnStateChangedListener>()

    private fun buildConfig(recorder: MediaRecorder) {
        val builder = recorderBuilder
        if (builder != null) {
            builder.setRecorderConfig(recorder)
            return
        }
        if (!checkPermission()) {
            mediaRecorder = null
            return
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD)
        recorder.setAudioChannels(2)
        recorderCacheDir.mkdirs()
        val outFile = File(
            recorderCacheDir,
            java.lang.Long.toHexString(System.currentTimeMillis()) + suffix
        )
        recorder.setOutputFile(outFile.path)
        currentCacheFile = outFile
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
        duration = 0L
        mediaRecorder = recorder
        buildConfig(recorder)
        mediaRecorder?.prepare()
        changeState(State.CREATED)
    }

    fun start() {
        init()
        if (state.isAtLeast(State.RESUMED)) {
            updateDuration()
        } else {
            lastTime = System.currentTimeMillis()
        }
        changeState(State.STARTED)
        changeState(State.RESUMED)
        mediaRecorder?.start()
        isRecording = true
    }

    fun resume() {
        if (state.isAtLeast(State.RESUMED)) {
            updateDuration()
        } else {
            lastTime = System.currentTimeMillis()
        }
        changeState(State.RESUMED)
        mediaRecorder?.resume()
        isRecording = true
    }

    fun pause() {
        if (state.isAtLeast(State.RESUMED)) {
            updateDuration()
        }
        changeState(State.STARTED)
        mediaRecorder?.pause()
        isRecording = false
    }

    private fun updateDuration() {
        if (lastTime > 0L) {
            val now = System.currentTimeMillis()
            duration += now - lastTime
            lastTime = now
        }
    }

    fun getDuration(): Long {
        if (state.isAtLeast(State.RESUMED)) {
            updateDuration()
        }
        return duration
    }

    fun tryPause() {
        if (state.isAtLeast(State.STARTED)) {
            pause()
        }
    }

    fun tryResume() {
        if (state.isAtLeast(State.STARTED)) {
            resume()
        }
    }

    fun stop() {
        if (state.isAtLeast(State.RESUMED)) {
            updateDuration()
        }
        changeState(State.INITIALIZED)
        mediaRecorder?.stop()
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null
        isRecording = false
    }

    fun addStateListener(listener: OnStateChangedListener) {
        listenerListener.add(listener)
    }

    fun removeListener(listener: OnStateChangedListener) {
        listenerListener.remove(listener)
    }

    private fun checkPermission(): Boolean {
        val result = checkPermission(context)
        if (!result) {
            Log.e(
                "RecorderHelper",
                "Manifest.permission.RECORD_AUDIO != PackageManager.PERMISSION_GRANTED "
            )
        }
        return result
    }

    private fun changeState(newState: State) {
        this.state = newState
        listenerListener.forEach {
            it.onRecorderStateChanged(this, newState)
        }
    }

    /**
     * 这是一个同步的方法
     * 需要在子线程中执行
     */
    fun saveTo(file: File): Boolean {
        val cache = currentCacheFile ?: return false
        return copy(cache, file)
    }

    val isRunning: Boolean
        get() {
            return mediaRecorder != null && isRecording
        }

    fun setRecorderBuilder(builder: RecorderBuilder?) {
        this.recorderBuilder = builder
    }

    fun interface RecorderBuilder {
        fun setRecorderConfig(recorder: MediaRecorder)
    }

    enum class State {
        INITIALIZED,
        CREATED,
        STARTED,
        RESUMED;

        fun isAtLeast(state: State): Boolean {
            return this.ordinal >= state.ordinal
        }

    }

    fun interface OnStateChangedListener {
        fun onRecorderStateChanged(recorder: RecorderHelper, state: State)
    }

}