package com.lollipop.techo.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.lollipop.base.listener.BackPressHandler
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

        val LAUNCHER: Class<out ActivityResultContract<Boolean, File?>> = ResultContract::class.java

        private fun autoRecord(intent: Intent) {
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

    private val recorderHelper = RecorderHelper(this).apply {
        addStateListener(::onRecorderStateChanged)
    }

    private var isSaving = false

    private var progressDrawable: CircularProgressDrawable? = null

    private val backPressHandler = BackPressHandler(true) {
        dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowInsetsHelper.initWindowFlag(this)
        backPressHandler.bindTo(this)
        setContentView(binding.root)
        binding.dialogRootView.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)
        binding.backgroundView.onClick {
            cancel()
        }
        binding.dialogRootView.setEmptyClick()
        dialogAnimationHelper.preload()
        dialogAnimationHelper.isNeedPost = true

        binding.recorderWaveView.setWaveProvider(::getAmplitude)

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

        setResult(Activity.RESULT_CANCELED)
    }

    private fun getAmplitude(): Float {
        updateDuration()
        return recorderHelper.getAmplitude()
    }

    @SuppressLint("SetTextI18n")
    private fun updateDuration() {
        val duration = recorderHelper.getDuration()
        val sAll = duration / 1000
        val m = sAll / 60
        val s = sAll % 60
        val builder = StringBuilder()
        if (m < 10) {
            builder.append("0")
        }
        builder.append(m)
        builder.append(":")
        if (s < 10) {
            builder.append("0")
        }
        builder.append(s)
        tryUI {
            binding.durationView.text = builder.toString()
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

    override fun onPause() {
        super.onPause()
        recorderHelper.tryPause()
    }

    override fun onResume() {
        super.onResume()
        recorderHelper.tryResume()
    }

    private fun isRunning(): Boolean {
        return recorderHelper.isRunning
    }

    private fun save() {
        doAsync {
            val fileName =
                java.lang.Long.toHexString(System.currentTimeMillis()) + recorderHelper.suffix
            val file = File(audioDir, fileName)
            val saveResult = recorderHelper.saveTo(file)
            onUI {
                saveEnd(saveResult, file)
            }
        }
    }

    private fun onRecorderStateChanged(recorder: RecorderHelper, state: RecorderHelper.State) {
        if (state.isAtLeast(RecorderHelper.State.RESUMED)) {
            binding.recorderWaveView.start()
        } else {
            binding.recorderWaveView.stop()
        }
    }

    private fun saveEnd(success: Boolean, audioFile: File) {
        if (isFinishing || isDestroyed) {
            return
        }
        isSaving = false
        if (success) {
            setResult(Activity.RESULT_OK, Intent().apply {
                putAudioFile(this, audioFile.path)
            })
            dismiss()
        } else {
            Toast.makeText(this, R.string.recorder_error, Toast.LENGTH_SHORT).show()
            updateRecordButton()
        }
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

    override fun onDestroy() {
        binding.recorderWaveView.stop()
        super.onDestroy()
        recorderHelper.stop()
    }

    class ResultContract : ActivityResultContract<Boolean, File?>() {

        override fun createIntent(context: Context, input: Boolean): Intent {
            return Intent(context, RecorderActivity::class.java).apply {
                if (input) {
                    autoRecord(this)
                }
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): File? {
            intent ?: return null
            if (resultCode != Activity.RESULT_OK) {
                return null
            }
            val audioFile = getAudioFile(intent)
            if (audioFile.isEmpty()) {
                return null
            }
            val file = File(audioFile)
            if (!file.exists()) {
                return null
            }
            return file
        }

    }

}