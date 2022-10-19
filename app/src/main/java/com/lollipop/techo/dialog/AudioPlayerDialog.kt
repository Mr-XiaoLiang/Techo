package com.lollipop.techo.dialog

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lollipop.base.util.lazyBind
import com.lollipop.recorder.AudioPlayerHelper
import com.lollipop.recorder.AudioPlayerHelper.State.PREPARED
import com.lollipop.techo.databinding.DialogAudioPlayBinding

class AudioPlayerDialog(
    context: Context,
    private val filePath: String
) : BottomSheetDialog(context), AudioPlayerHelper.OnPlayerStateChangedListener {

    private val binding: DialogAudioPlayBinding by lazyBind()

    private val player = AudioPlayerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initPlayer()
    }

    private fun initView() {
        // TODO
    }

    private fun initPlayer() {
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
            else -> {

            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        player.stop()
    }

}