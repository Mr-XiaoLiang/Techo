package com.lollipop.techo.activity

import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.setEmptyClick
import com.lollipop.recorder.AudioRecorder
import com.lollipop.recorder.RecorderConfig
import com.lollipop.techo.R
import com.lollipop.techo.databinding.ActivityRecorderBinding
import com.lollipop.techo.util.AnimationHelper
import java.io.File

class RecorderActivity : BaseActivity() {

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
    }

    private fun cancel() {
        recorder.pause()
        recorder.cleanCache()
        dismiss()
    }

    private fun onRecordButtonClick() {
        if (recorder.isRunning) {
            recorder.save(getAudioFile())
        } else {
            recorder.start()
        }
        updateRecordButton()
    }

    private fun getAudioFile(): File {
        TODO()
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

}