package com.lollipop.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.audiofx.NoiseSuppressor
import androidx.core.app.ActivityCompat
import com.lollipop.recorder.wave.WaveHelper
import java.io.File
import java.io.FileOutputStream
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
        ): RecordResult {
            // 一次数据的数量
            var size = max(config.bufferSizeInBytes, 1)
            // 数据块单元的数量
            var blockSize = 1
            // 如果是16bit，那么一个数据是2个byte，所以是双倍
            if (config.is16Bit) {
                blockSize *= 2
            }
            // 块的数量和声道数量是成倍关系的
            blockSize *= record.channelCount
            // 最后，我们判断是不是有余量来对齐数据块，如果可以，那么我们每次都拿整块的数据
            // 做减法的原因在于，缓存池可能没有那么大，我们只能少拿点
            if (size > blockSize && size % blockSize != 0) {
                size -= (size % blockSize)
            }
            // 创建一个缓冲
            val buffer = ByteArray(size)

            // 通知格式发生变化
            listener.forEach { it.onFormatChanged(config.is16Bit, record.channelCount) }

            // 断开的话我们要尽快停止
            while (canNext()) {
                // 读取一个缓冲数据
                val result = record.read(buffer, 0, size)
                // 大于等于0才说明正确读取到了
                if (result >= 0) {
                    // 先做写入
                    outputStream.write(buffer, 0, result)
                    // 再把数据扔出去
                    listener.forEach { it.onRecord(buffer, 0, result) }
                } else {
                    when (result) {
                        AudioRecord.ERROR -> {
                            return RecordResult.GenericError()
                        }
                        AudioRecord.ERROR_BAD_VALUE -> {
                            return RecordResult.BadValueError()
                        }
                        AudioRecord.ERROR_DEAD_OBJECT -> {
                            return RecordResult.DeadObjectError()
                        }
                        AudioRecord.ERROR_INVALID_OPERATION -> {
                            return RecordResult.InvalidOperationError()
                        }
                    }
                }
            }
            outputStream.flush()
            return RecordResult.Success()
        }
    }

    private var isInit = false

    private var audioRecord: AudioRecord? = null

    private var cacheRoot = ""

    private var enableNoiseSuppressor = false

    private var noiseSuppressor: NoiseSuppressor? = null

    private var status = Status.NO_READY

    private val cacheFiles = ArrayList<File>()

    private val recorderListenerList = ArrayList<RecorderListener>()
    private val recordStatusListenerList = ArrayList<RecordStatusListener>()

    /**
     * 波形辅助器
     */
    val wave = WaveHelper(config)

    val hasCache: Boolean
        get() {
            return cacheFiles.isNotEmpty()
        }

    init {
        recorderListenerList.add(wave)
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

    private class RecordThread(
        private val config: RecorderConfig,
        private val record: AudioRecord,
        private val outputStream: OutputStream,
        private val canNext: () -> Boolean,
        private val listener: Array<RecorderListener>,
        private val statusListener: Array<RecordStatusListener>
    ) : Thread() {

        companion object {
            fun start(
                config: RecorderConfig,
                record: AudioRecord,
                cacheFile: File,
                listener: Array<RecorderListener>,
                statusListener: Array<RecordStatusListener>,
                canNext: () -> Boolean
            ) {
                val fileOutputStream = FileOutputStream(cacheFile)
                RecordThread(
                    config,
                    record,
                    fileOutputStream,
                    canNext,
                    listener,
                    statusListener
                ).start()
            }
        }

        override fun run() {
            statusListener.forEach { it.onRecordStart() }
            val result = record(config, record, outputStream, canNext, listener)
            statusListener.forEach { it.onRecordStop(result) }
        }

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