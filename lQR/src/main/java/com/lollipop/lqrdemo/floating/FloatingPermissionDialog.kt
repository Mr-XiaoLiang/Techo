package com.lollipop.lqrdemo.floating

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.net.toUri
import com.lollipop.base.util.changeAlpha
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.base.BaseBottomDialog
import com.lollipop.lqrdemo.databinding.ActivityFloatingPermissionBinding
import com.lollipop.pigment.Pigment

class FloatingPermissionDialog(
    context: Context,
    private val authorizedPermission: Array<PermissionType>,
    private val onRequestClick: (PermissionType) -> Unit
) : BaseBottomDialog(context) {

    private val binding: ActivityFloatingPermissionBinding by lazyBind()

    override val contentView: View
        get() {
            return binding.root
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.floatingPermissionStatusIcon.setImageResource(getPermissionStatus(PermissionType.Floating))
        binding.recordingPermissionStatusIcon.setImageResource(getPermissionStatus(PermissionType.Recording))
        binding.floatingPermissionButton.onClick {
            onRequestClick(PermissionType.Floating)
        }
        binding.recordingPermissionButton.onClick {
            onRequestClick(PermissionType.Recording)
        }
    }

    private fun getPermissionStatus(permission: PermissionType): Int {
        val hasPermission = hasPermission(permission)
        return if (hasPermission) {
            R.drawable.ic_baseline_done_24
        } else {
            R.drawable.ic_baseline_warning_amber_24
        }
    }

    private fun hasPermission(permission: PermissionType): Boolean {
        for (per in authorizedPermission) {
            if (per == permission) {
                return true
            }
        }
        return false
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.backgroundColor)

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

    enum class PermissionType {
        Floating,
        Recording
    }

}