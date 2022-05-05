package com.lollipop.recorder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.audiofx.NoiseSuppressor
import androidx.core.app.ActivityCompat
import com.lollipop.recorder.AudioRecorder.Task.Companion.start
import com.lollipop.recorder.encode.DefaultEncoderProvider
import com.lollipop.recorder.encode.PcmEncoder
import com.lollipop.recorder.wave.WaveHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.max


class AudioRecorder(
    val config: RecorderConfig,
    val cacheDir: File,
) {

    companion object {

        private val AudioRecord?.isRunning: Boolean
            get() {
                return this?.recordingState == AudioRecord.RECORDSTATE_RECORDING
            }

        private val AudioRecord?.isInit: Boolean
            get() {
                return this?.state == AudioRecord.STATE_INITIALIZED
            }

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
            recordSwitch: RecordSwitch,
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
            while (recordSwitch.canNext && record.isRunning) {
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

        var DEFAULT = DefaultEncoderProvider()
    }

    private var isInit = false

    private var audioRecord: AudioRecord? = null

    private var cacheRoot = ""

    private var enableNoiseSuppressor = false

    private var noiseSuppressor: NoiseSuppressor? = null

    private val cacheFiles = ArrayList<File>()

    private val recorderListenerList = ArrayList<RecorderListener>()
    private val recordStatusListenerList = ArrayList<RecordStatusListener>()
    private val recordSaveListenerList = ArrayList<RecordSaveListener>()

    /**
     * 波形辅助器
     */
    val wave = WaveHelper(config)

    val hasCache: Boolean
        get() {
            return cacheFiles.isNotEmpty()
        }

    /**
     * 默认的编码器提供者
     */
    var encoderProvider = DEFAULT

    val isRunning: Boolean
        get() {
            return audioRecord?.isRunning ?: false
        }

    private var recordSwitch: RecordStatusSwitch? = null

    init {
        addRecorderListener(wave)
    }

    @SuppressLint("MissingPermission")
    private fun init() {
        if (audioRecord.isInit) {
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
        isInit = true
    }

    fun addRecorderListener(listener: RecorderListener) {
        recorderListenerList.add(listener)
    }

    fun removeRecorderListener(listener: RecorderListener) {
        recorderListenerList.remove(listener)
    }

    fun addRecordStatusListener(listener: RecordStatusListener) {
        recordStatusListenerList.add(listener)
    }

    fun removeRecordStatusListener(listener: RecordStatusListener) {
        recordStatusListenerList.remove(listener)
    }

    fun addRecordSaveListener(listener: RecordSaveListener) {
        recordSaveListenerList.add(listener)
    }

    fun removeRecordSaveListener(listener: RecordSaveListener) {
        recordSaveListenerList.remove(listener)
    }

    fun enableNoiseSuppressor(enable: Boolean) {
        enableNoiseSuppressor = NoiseSuppressor.isAvailable() && enable
        val recorder = audioRecord
        if (recorder == null || !recorder.isRunning) {
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
        init()
        val recorder = audioRecord ?: return
        if (recorder.isRunning) {
            return
        }
        recordSwitch?.stop()
        val recordStatusSwitch = RecordStatusSwitch()
        recordSwitch = recordStatusSwitch
        val cacheFile = File(cacheDir, cacheRoot + cacheFiles.size)
        cacheFiles.add(cacheFile)
        recorder.startRecording()
        if (enableNoiseSuppressor) {
            enableNoiseSuppressor(enableNoiseSuppressor)
        }
        RecordThread.start(
            config,
            recorder,
            cacheFile,
            recorderListenerList,
            recordStatusListenerList,
            recordStatusSwitch
        )
    }

    fun pause() {
        // 关闭任务
        recordSwitch?.stop()
        audioRecord?.stop()
    }

    fun save(file: File) {
        if (!hasCache) {
            return
        }
        val record = audioRecord ?: return
        if (record.isRunning) {
            pause()
        }
        val dir = file.parentFile ?: return
        if (!dir.exists()) {
            dir.mkdirs()
        }
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        val outputStream = FileOutputStream(file)
        val encoder = encoderProvider.getEncoder(config, record, outputStream)
        SaveThread(
            cacheFiles.toTypedArray(),
            recordSaveListenerList.toTypedArray(),
            encoder
        ).start()
    }

    fun destroy() {
        try {
            audioRecord?.release()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        audioRecord = null
    }

    fun cleanCache() {
        cacheFiles.forEach {
            try {
                it.delete()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private class RecordThread(
        private val config: RecorderConfig,
        private val record: AudioRecord,
        private val outputStream: OutputStream,
        private val recordSwitch: RecordSwitch,
        private val listener: Array<RecorderListener>,
        private val statusListener: Array<RecordStatusListener>
    ) : Task, RecordSwitch {

        companion object {
            fun start(
                config: RecorderConfig,
                record: AudioRecord,
                cacheFile: File,
                listener: List<RecorderListener>,
                statusListener: List<RecordStatusListener>,
                recordSwitch: RecordSwitch
            ) {
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }
                cacheFile.createNewFile()
                RecordThread(
                    config,
                    record,
                    FileOutputStream(cacheFile),
                    recordSwitch,
                    listener.toTypedArray(),
                    statusListener.toTypedArray()
                ).start()
            }
        }

        private var isActive = true

        override fun doTask() {
            statusListener.forEach { it.onRecordStart() }
            val result = try {
                record(config, record, outputStream, this, listener)
            } catch (e: Throwable) {
                e.printStackTrace()
                RecordResult.GenericError(msg = e.message ?: "", obj = e)
            }
            try {
                outputStream.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            statusListener.forEach { it.onRecordStop(result) }
        }

        override fun onError(e: Throwable, result: RecordResult) {
            super.onError(e, result)
            statusListener.forEach { it.onRecordStop(result) }
        }

        override val canNext: Boolean
            get() {
                // 如果关闭一次，那么任务就永远终止
                if (isActive) {
                    val next = recordSwitch.canNext
                    isActive = next
                    return next
                }
                return false
            }

    }

    private class SaveThread(
        private val cacheFile: Array<File>,
        private val listener: Array<RecordSaveListener>,
        private val encoder: PcmEncoder,
    ) : Task {

        override fun doTask() {
            val fileCount = cacheFile.size
            listener.forEach { it.onSaveStart() }
            val buffer = ByteArray(1024)
            cacheFile.forEachIndexed { index, file ->
                val fileLength = file.length()
                var writeLength = 0L
                val inputStream = FileInputStream(file)
                var readLength = inputStream.read(buffer)
                while (readLength >= 0) {
                    encoder.write(buffer, 0, readLength)
                    writeLength += readLength
                    listener.forEach {
                        it.onSaveProgressChanged(
                            getProgress(
                                fileCount,
                                index,
                                fileLength,
                                writeLength
                            )
                        )
                    }
                    readLength = inputStream.read(buffer)
                }
            }
            encoder.flush()
            encoder.destroy()
            val result = RecordResult.Success()
            listener.forEach { it.onSaveEnd(result) }
        }

        override fun onError(e: Throwable, result: RecordResult) {
            super.onError(e, result)
            listener.forEach { it.onSaveEnd(result) }
        }

        private fun getProgress(
            fileCount: Int,
            fileIndex: Int,
            fileLength: Long,
            writeLength: Long
        ): Float {
            val countProgress = fileIndex * 1F / fileCount
            val fileProgress = max(writeLength * 1F / fileLength, 1F) / fileCount
            return countProgress + fileProgress
        }

    }

    private class RecordStatusSwitch : RecordSwitch {

        private var curreyStatus: Status = Status.START

        override val canNext: Boolean
            get() {
                return curreyStatus.isRunning
            }

        fun stop() {
            curreyStatus = Status.PAUSE
        }

    }

    private interface RecordSwitch {
        val canNext: Boolean
    }

    /**
     * 录音对象的状态
     */
    enum class Status {
        /**
         * 录音
         */
        START,

        /**
         * 暂停
         */
        PAUSE;

        val isRunning: Boolean
            get() {
                return this == START
            }
    }

    private interface Task {

        companion object {
            fun Task.start() {
                Thread(TaskRunnable(this)).start()
            }
        }

        fun onError(e: Throwable, result: RecordResult) {

        }

        fun doTask()

    }

    private class TaskRunnable(private val task: Task) : Runnable {
        override fun run() {
            try {
                task.doTask()
            } catch (e: Throwable) {
                e.printStackTrace()
                task.onError(e, RecordResult.GenericError.create(e))
            }
        }
    }

}