package com.lollipop.techo.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import com.lollipop.base.util.lazyBind
import com.lollipop.recorder.AudioPlayerHelper
import com.lollipop.recorder.AudioPlayerHelper.State.*
import com.lollipop.recorder.AudioVisualizerView
import com.lollipop.recorder.VisualizerHelper
import com.lollipop.techo.databinding.DialogAudioPlayBinding
import kotlin.math.max
import kotlin.math.min

class AudioPlayerDialog(
    context: Context,
    private val filePath: String
) : BaseBottomSheetDialog(context),
    AudioPlayerHelper.OnPlayerStateChangedListener,
    AudioVisualizerView.OnSeekChangedCallback {

    companion object {
        private const val TIME_UPDATE_DELAY = 100L
        private const val EXPECTED_CAPTURE_SIZE = 64 * 2
    }

    private val binding: DialogAudioPlayBinding by lazyBind()

    private val player = AudioPlayerHelper()

    private val visualizerHelper = VisualizerHelper.createWith(
        player,
        VisualizerHelper.Capture(frequency = true),
        captureSize = min(
            VisualizerHelper.captureSizeRange.last,
            max(
                VisualizerHelper.captureSizeRange.first,
                EXPECTED_CAPTURE_SIZE
            )
        )
    )

    private val updateTimeTask = Runnable {
        updateTime()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initPlayer()
    }

    private fun initView() {
        binding.visualizerView.onSeekChanged(this)
        visualizerHelper.addRenderer(binding.visualizerView)
    }

    @SuppressLint("SetTextI18n")
    private fun updateTime() {
        val currentPosition = player.currentPosition
        val allS = currentPosition / 1000
        val s = (allS % 60).coerceAtLeast(0)
        val m = (allS / 60).coerceAtLeast(0)
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
        binding.durationView.text = builder.toString()

        val progress = currentPosition * 1F / player.duration
        binding.visualizerView.onProgressChanged(progress)

        binding.durationView.postDelayed(updateTimeTask, TIME_UPDATE_DELAY)
    }

    private fun stopTimeUpdate() {
        binding.durationView.removeCallbacks(updateTimeTask)
    }

    private fun initPlayer() {
        if (filePath.isEmpty()) {
            return
        }
        player.addStateChangedListener(this)
        player.setDataSource {
            it.setDataSource(filePath)
        }
        player.isLooping = true
    }

    override fun onAudioPlayerStateChanged(
        helper: AudioPlayerHelper,
        state: AudioPlayerHelper.State
    ) {
        when (state) {
            PREPARED -> {
                player.start()
            }
            STARTED -> {
                updateTime()
            }
            PAUSED, STOP -> {
                stopTimeUpdate()
            }
            else -> {

            }
        }
    }

    override fun onSeekChanged(progress: Float) {
        val millisecond = (player.duration * progress).toLong().coerceAtLeast(0)
        player.seekTo(millisecond)
    }

    override fun dismiss() {
        super.dismiss()
        player.stop()
        player.release()
        visualizerHelper.release()
        stopTimeUpdate()
    }


}