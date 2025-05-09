package com.lollipop.lqrdemo.floating

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import com.lollipop.base.util.changeAlpha
import com.lollipop.base.util.lazyBind
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.fixInsetsByPadding
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.databinding.ActivityFloatingPermissionBinding
import com.lollipop.pigment.Pigment

class FloatingPermissionActivity : ColorModeActivity() {

    companion object {

        private fun requestFloatingPermission(context: Context) {
            // 引导用户去设置页面开启权限
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = "package:${context.packageName}".toUri()
            context.startActivity(intent)
        }

        private fun checkFloatingPermission(context: Context): Boolean {
            return Settings.canDrawOverlays(context);
        }

        private fun checkRecordingPermission(context: Context): Boolean {
            return Settings.canDrawOverlays(context);
        }

        fun check(context: Context): Boolean {
            start(context)
            return false
        }

        private fun start(context: Context) {
            val intent = Intent(context, FloatingPermissionActivity::class.java)
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

    }

    private val binding: ActivityFloatingPermissionBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.root.fixInsetsByPadding(WindowInsetsEdge.Companion.ALL)
        bindByBack(binding.backButton)


    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.backgroundColor)
        binding.backButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.titleView.setTextColor(pigment.onBackgroundTitle)

        binding.floatingPermissionTitle.setTextColor(pigment.onBackgroundTitle)
        binding.floatingPermissionStatusIcon.imageTintList =
            ColorStateList.valueOf(pigment.secondaryColor)
        binding.floatingPermissionSummary.setTextColor(pigment.onBackgroundBody)

        binding.recordingPermissionStatusIcon.imageTintList =
            ColorStateList.valueOf(pigment.secondaryColor)
        binding.recordingPermissionTitle.setTextColor(pigment.onBackgroundTitle)
        binding.recordingPermissionSummary.setTextColor(pigment.onBackgroundBody)

        binding.floatingPermissionButton.setBackgroundColor(pigment.primaryColor.changeAlpha(0.2F))
        binding.recordingPermissionButton.setBackgroundColor(pigment.primaryColor.changeAlpha(0.2F))
    }

}