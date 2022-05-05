package com.lollipop.techo.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.*
import com.lollipop.recorder.*
import com.lollipop.techo.R
import com.lollipop.techo.databinding.ActivityRecorderBinding
import com.lollipop.techo.drawable.CircularProgressDrawable
import com.lollipop.techo.util.AnimationHelper
import java.io.File

class RecorderActivity : BaseActivity(),
    RecordStatusListener,
    RecordSaveListener {

    companion object {

        private const val PARAMS_AUDIO_FILE = "RECORD_AUDIO_FILE"

        private const val PARAMS_AUTO_RECORD = "AUTO_RECORD"

        fun autoRecord(intent: Intent) {
            intent.putExtra(PARAMS_AUTO_RECORD, true)
        }

        fun getAudioFile(intent: Intent?): String {
            return intent?.getStringExtra(PARAMS_AUDIO_FILE) ?: ""
        }

        private fun putAudioFile(intent: Intent, file: String) {
            intent.putExtra(PARAMS_AUDIO_FILE, file)
        }

    }

    private val binding: ActivityRecorderBinding by lazyBind()

    private val dialogAnimationHelper = AnimationHelper(
        onUpdate = ::onDialogAnimationUpdate
    ).apply {
        onStart(::onDialogAnimationStart)
        onEnd(::onDialogAnimationEnd)
    }

    private val isAutoRecord: Boolean
        get() {
            return intent.getBooleanExtra(PARAMS_AUTO_RECORD, false)
        }

    private val recorder by lazy {
        AudioRecorder(RecorderConfig.create(), cacheDir)
    }

    private val audioDir: File by lazy {
        File(filesDir, "recorder")
    }

    private val audioFile: File by lazy {
        File(audioDir, "audio_${System.currentTimeMillis()}")
    }

    private var isSaving = false

    private var progressDrawable: CircularProgressDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        binding.dialogRootView.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)
        binding.backgroundView.setOnClickListener {
            cancel()
        }
        binding.dialogRootView.setEmptyClick()
        dialogAnimationHelper.preload()
        dialogAnimationHelper.isNeedPost = true

        binding.recorderCloseBtn.setOnClickListener {
            cancel()
        }

        binding.recorderMicView.setOnClickListener {
            onRecordButtonClick()
        }

        initRecorder()

        dialogAnimationHelper.open(true)

        updateRecordButton()

        binding.recorderMicView.post {
            if (isAutoRecord) {
                binding.recorderMicView.callOnClick()
            }
        }
    }

    private fun initRecorder() {
        recorder.wave.apply {
            waveListener = binding.recorderWaveView
            enable = true
        }
        recorder.addRecordSaveListener(this)
        recorder.addRecordStatusListener(this)
    }

    private fun cancel() {
        recorder.pause()
        recorder.cleanCache()
        dismiss()
    }

    private fun onRecordButtonClick() {
        if (recorder.isRunning) {
            recorder.save(audioFile)
        } else {
            recorder.start()
        }
        updateRecordButton()
    }

    private fun updateRecordButton() {
        if (isFinishing || isDestroyed) {
            return
        }
        tryUI {
            when {
                isSaving -> {
                    if (progressDrawable == null) {
                        progressDrawable = CircularProgressDrawable()
                    }
                    binding.recorderMicView.setText(R.string.saving)
                    binding.recorderMicView.icon = progressDrawable
                    progressDrawable?.start()
                }
                recorder.isRunning -> {
                    binding.recorderMicView.setText(R.string.stop)
                    binding.recorderMicView.setIconResource(R.drawable.ic_baseline_stop_24)
                }
                else -> {
                    binding.recorderMicView.setText(R.string.start)
                    binding.recorderMicView.setIconResource(R.drawable.ic_baseline_mic_24)
                }
            }
        }
    }

    private fun onDialogAnimationUpdate(progress: Float) {
        binding.dialogRootView.translationY = binding.dialogRootView.height * (1 - progress)
        binding.backgroundView.alpha = progress
    }

    private fun dismiss() {
        dialogAnimationHelper.toClose(true)
    }

    private fun onDialogAnimationStart(progress: Float) {
        binding.root.isVisible = true
    }

    private fun onDialogAnimationEnd(progress: Float, isPreload: Boolean) {
        if (isPreload) {
            binding.root.isInvisible = true
        } else if (dialogAnimationHelper.progressIs(AnimationHelper.PROGRESS_MIN)) {
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 更新intent
        setIntent(intent)
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder.pause()
        recorder.cleanCache()
        recorder.destroy()
    }

    override fun onSaveStart() {
        isSaving = true
        if (isDestroyed || isFinishing) {
            return
        }
        tryUI {
            progressDrawable?.start()
        }
    }

    override fun onSaveProgressChanged(progress: Float) {
        if (isDestroyed || isFinishing) {
            return
        }
        tryUI {
            progressDrawable?.progress = progress
        }
    }

    override fun onSaveEnd(result: RecordResult) {
        isSaving = false
        if (isDestroyed || isFinishing) {
            return
        }
        tryUI {
            progressDrawable?.stop()
            updateStatusView(result)
            when (result) {
                is RecordResult.Success -> {
                    setResult(
                        RESULT_OK,
                        Intent().apply {
                            putAudioFile(this, audioFile.path)
                        }
                    )
                    dismiss()
                }
                else -> {
                }
            }
        }
    }

    override fun onRecordStart() {
        tryUI {
            updateRecordButton()
        }
    }

    override fun onRecordStop(result: RecordResult) {
        tryUI {
            updateRecordButton()
            updateStatusView(result)
        }
    }

    private fun updateStatusView(result: RecordResult) {
        val isSuccess = result is RecordResult.Success
        binding.statusPanel.isVisible = !isSuccess
        if (isSuccess) {
            return
        }
        val errorType: Int
        val errorCode: Int
        val errorMsg: String
        when (result) {
            is RecordResult.BadValueError -> {
                errorCode = result.code
                errorMsg = result.msg
                errorType = R.string.record_result_error_bad_value
            }
            is RecordResult.DeadObjectError -> {
                errorCode = result.code
                errorMsg = result.msg
                errorType = R.string.record_result_error_dead_object
            }
            is RecordResult.GenericError -> {
                errorCode = result.code
                errorMsg = result.msg
                errorType = R.string.record_result_error_generic
            }
            is RecordResult.InvalidOperationError -> {
                errorCode = result.code
                errorMsg = result.msg
                errorType = R.string.record_result_error_invalid_operation
            }
            is RecordResult.Success -> {
                errorCode = 0
                errorMsg = ""
                errorType = 0
                // do nothing
            }
        }
        binding.statusMsgView.text = getString(
            R.string.record_result_error,
            getString(errorType),
            errorCode,
            errorMsg
        )
    }

}