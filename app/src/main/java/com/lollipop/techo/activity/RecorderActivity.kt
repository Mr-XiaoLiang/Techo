package com.lollipop.techo.activity

import android.os.Bundle
import com.lollipop.base.ui.BaseActivity
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.setEmptyClick
import com.lollipop.techo.databinding.ActivityRecorderBinding
import com.lollipop.techo.util.AnimationHelper

class RecorderActivity : BaseActivity() {

    private val binding: ActivityRecorderBinding by lazyBind()

    private val dialogAnimationHelper = AnimationHelper(onUpdate = ::onDialogAnimationUpdate).apply {
        onEnd(::onDialogAnimationEnd)
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
        dialogAnimationHelper.isNeedPost = true
        dialogAnimationHelper.open(true)
    }

    private fun onDialogAnimationUpdate(progress: Float) {
        binding.dialogRootView.translationY = binding.dialogRootView.height * (1 - progress)
        binding.backgroundView.alpha = progress
    }

    private fun dismiss() {
        dialogAnimationHelper.toClose(true)
    }

    private fun onDialogAnimationEnd(progress: Float) {
        if (dialogAnimationHelper.progressIs(AnimationHelper.PROGRESS_MIN)) {
            finish()
        }
    }

}