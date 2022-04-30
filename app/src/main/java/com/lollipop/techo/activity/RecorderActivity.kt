package com.lollipop.techo.activity

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
import com.lollipop.techo.util.AnimationHelper
import java.io.File

class RecorderActivity : BaseActivity(),
    RecordStatusListener,
    RecordSaveListener {

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
        binding.recorderMicView.setText(
            if (recorder.isRunning) {
                R.string.stop
            } else {
                R.string.start
            }
        )
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

    override fun onSaveStart() {
        TODO("Not yet implemented")
    }

    override fun onSaveProgressChanged(progress: Float) {
        TODO("Not yet implemented")
    }

    override fun onSaveEnd(result: RecordResult) {
        when (result) {
            is RecordResult.Success -> {
                TODO()
            }
            is RecordResult.BadValueError -> TODO()
            is RecordResult.DeadObjectError -> TODO()
            is RecordResult.GenericError -> TODO()
            is RecordResult.InvalidOperationError -> TODO()
        }
        TODO("Not yet implemented")
    }

    override fun onRecordStart() {
        TODO("Not yet implemented")
    }

    override fun onRecordStop(result: RecordResult) {
        when (result) {
            is RecordResult.Success -> {
                TODO()
            }
            is RecordResult.BadValueError -> TODO()
            is RecordResult.DeadObjectError -> TODO()
            is RecordResult.GenericError -> TODO()
            is RecordResult.InvalidOperationError -> TODO()
        }
        TODO("Not yet implemented")
    }

}