package com.lollipop.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.audiofx.NoiseSuppressor
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.OutputStream
import kotlin.math.max


class AudioRecorder(
    val config: RecorderConfig,
    val cacheDir: File,
) {

    companion object {
        fun checkPermission(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }

        private fun record(
            config: RecorderConfig,
            record: AudioRecord,
            outputStream: OutputStream,
            canNext: () -> Boolean,
            listener: Array<RecorderListener>
        ) {
            val size = max(config.bufferSizeInBytes, 1)
            val buffer = ByteArray(size)
            while (canNext()) {
                val result = record.read(buffer, 0, size)
                if (result >= 0) {
                    outputStream.write(buffer, 0, result)
                    listener.forEach { it.onRecord(buffer, 0, result) }
                } else {
                    when (result) {
                        AudioRecord.ERROR -> {
                            // TODO
                        }
                        AudioRecord.ERROR_BAD_VALUE -> {
                            // TODO
                        }
                        AudioRecord.ERROR_DEAD_OBJECT -> {
                            // TODO
                        }
                        AudioRecord.ERROR_INVALID_OPERATION -> {
                            // TODO
                        }
                    }
                }
            }
            outputStream.flush()
        }


    }

    private var isInit = false

    private var audioRecord: AudioRecord? = null

    private var cacheRoot = ""

    private var enableNoiseSuppressor = false

    private var noiseSuppressor: NoiseSuppressor? = null

    private var status = Status.NO_READY

    private val cacheFiles = ArrayList<File>()

    /**
     * 波形辅助器
     */
    val waveHelper = WaveHelper()

    val hasCache: Boolean
        get() {
            return cacheFiles.isNotEmpty()
        }

    @SuppressLint("MissingPermission")
    private fun init() {
        if (isInit) {
            return
        }
        audioRecord = AudioRecord(
            config.source,
            config.sampleRateInHz,
            config.channelConfig,
            config.audioFormat,
            config.bufferSizeInBytes
        )
        if (cacheDir.isFile) {
            cacheDir.delete()
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        cacheRoot = "LAudio" + System.currentTimeMillis()
    }

    fun enableNoiseSuppressor(enable: Boolean) {
        enableNoiseSuppressor = NoiseSuppressor.isAvailable() && enable
        val recorder = audioRecord
        if (recorder == null || !status.isRunning) {
            return
        }
        if (enable) {
            if (noiseSuppressor != null) {
                return
            }
            noiseSuppressor = NoiseSuppressor.create(recorder.audioSessionId)
            noiseSuppressor?.enabled = true
        } else {
            noiseSuppressor?.enabled = false
            noiseSuppressor?.release()
            noiseSuppressor = null
        }
    }

    fun start() {
        TODO()
    }

    fun pause() {
        TODO()
    }

    fun save(file: File) {
        TODO()
    }

    /**
     * 录音对象的状态
     */
    enum class Status {
        /**
         * 未开始
         */
        NO_READY,

        /**
         * 预备
         */
        READY,

        /**
         * 录音
         */
        START,

        /**
         * 暂停
         */
        PAUSE,

        /**
         * 停止
         */
        STOP;

        val isRunning: Boolean
            get() {
                return this == START
            }
    }

}