package com.lollipop.techo.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.*
import com.lollipop.recorder.RecorderHelper
import com.lollipop.techo.R
import com.lollipop.techo.databinding.ActivityRecorderBinding
import com.lollipop.techo.drawable.CircularProgressDrawable
import com.lollipop.techo.util.AnimationHelper
import java.io.File

class RecorderActivity : BaseActivity() {

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

    private val audioDir: File by lazy {
        File(filesDir, "recorder")
    }

    private val recorderHelper = RecorderHelper(this)

    private var isSaving = false

    private var progressDrawable: CircularProgressDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        setContentView(binding.root)
        binding.dialogRootView.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)
        binding.backgroundView.onClick {
            cancel()
        }
        binding.dialogRootView.setEmptyClick()
        dialogAnimationHelper.preload()
        dialogAnimationHelper.isNeedPost = true

        binding.recorderCloseBtn.onClick {
            cancel()
        }

        binding.recorderMicView.onClick {
            onRecordButtonClick()
        }

        dialogAnimationHelper.open(true)

        updateRecordButton()

        binding.recorderMicView.post {
            if (isAutoRecord) {
                binding.recorderMicView.callOnClick()
            }
        }
    }

    private fun cancel() {
        recorderHelper.stop()
        dismiss()
    }

    private fun onRecordButtonClick() {
        if (isSaving) {
            return
        }
        if (isRunning()) {
            recorderHelper.stop()
            save()
        } else {
            recorderHelper.start()
        }
        updateRecordButton()
    }

    private fun isRunning(): Boolean {
        return recorderHelper.isRunning
    }

    private fun save() {
        // TODO()
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
                isRunning() -> {
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
        recorderHelper.stop()
    }

}