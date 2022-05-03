package com.lollipop.techo.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.setEmptyClick
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
            dismiss()
        }
        binding.dialogRootView.setEmptyClick()
        dialogAnimationHelper.preload()
        dialogAnimationHelper.isNeedPost = true

        binding.recorderCancelView.setOnClickListener {
            cancel()
        }

        binding.recorderMicView.setOnClickListener {
            onRecordButtonClick()
        }

        initRecorder()

        dialogAnimationHelper.open(true)

        updateRecordButton()
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
        progressDrawable?.start()
    }

    override fun onSaveProgressChanged(progress: Float) {
        if (isDestroyed || isFinishing) {
            return
        }
        progressDrawable?.progress = progress
    }

    override fun onSaveEnd(result: RecordResult) {
        isSaving = false
        if (isDestroyed || isFinishing) {
            return
        }


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
            is RecordResult.BadValueError -> TODO()
            is RecordResult.DeadObjectError -> TODO()
            is RecordResult.GenericError -> TODO()
            is RecordResult.InvalidOperationError -> TODO()
        }
    }

    override fun onRecordStart() {
        updateRecordButton()
    }

    override fun onRecordStop(result: RecordResult) {
        updateRecordButton()
    }

}