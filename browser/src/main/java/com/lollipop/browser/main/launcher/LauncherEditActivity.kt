package com.lollipop.browser.main.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import com.lollipop.base.util.ActivityLauncherHelper
import com.lollipop.base.util.lazyBind
import com.lollipop.browser.R
import com.lollipop.browser.base.HeaderActivity
import com.lollipop.browser.databinding.ActivityLauncherEditBinding

class LauncherEditActivity : HeaderActivity() {

    companion object {

        private const val PARAMS_INFO_ID = "INFO_ID"
        private const val PARAMS_INFO_BODY = "INFO_BODY"

        val LAUNCHER = ActivityLauncherHelper.launcher<ResultContract, LauncherInfo?, LauncherInfo?>()

        private fun putParams(intent: Intent, input: LauncherInfo?) {
            if (input != null) {
                intent.putExtra(PARAMS_INFO_ID, input.id)
                intent.putExtra(PARAMS_INFO_BODY, input.toString())
            }
        }

        private fun getParams(intent: Intent): LauncherInfo? {
            return LauncherInfo.parse(
                intent.getStringExtra(PARAMS_INFO_BODY) ?: ""
            ) {
                intent.getIntExtra(PARAMS_INFO_ID, 0)
            }
        }

    }

    private val binding: ActivityLauncherEditBinding by lazyBind()

    override val contentView: View
        get() {
            return binding.root
        }

    private val inputInfoId: Int by lazy {
        intent.getIntExtra(PARAMS_INFO_ID, 0)
    }
    private val inputInfoBody by lazy {
        val string = intent.getStringExtra(PARAMS_INFO_BODY) ?: ""
        LauncherInfo.parse(string) { 0 }
    }

    private var label = ""
    private var icon = ""

    @ColorInt
    private var iconTint = 0
    private var backgroundFile = ""
    private var backgroundColor = ArrayList<Int>()
    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isCreateMode = inputInfoId == 0
        initView(isCreateMode)
        setTitle(
            if (isCreateMode) {
                R.string.title_launch_create
            } else {
                R.string.title_launch_edit
            }
        )
    }

    private fun initView(isCreateMode: Boolean) {
        val infoBody = inputInfoBody
        if (isCreateMode || infoBody == null) {
            icon = ""
            iconTint = 0
            label = ""
            backgroundFile = ""
            backgroundColor.clear()
            url = ""
        } else {
            icon = infoBody.label
            iconTint = infoBody.iconTint
            label = infoBody.label
            backgroundFile = infoBody.backgroundFile?.path ?: ""
            backgroundColor.clear()
            backgroundColor.addAll(infoBody.backgroundColor)
            url = infoBody.url
        }
        updateInputView()
        updatePreview()
    }

    private fun updateInputView() {
        // TODO
    }

    private fun updatePreview() {
        // TODO
    }

    class ResultContract : ActivityLauncherHelper.Simple<LauncherInfo?, LauncherInfo?>() {

        override val activityClass: Class<out Activity> = LauncherEditActivity::class.java

        override fun putParams(intent: Intent, input: LauncherInfo?) {
            LauncherEditActivity.putParams(intent, input)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): LauncherInfo? {
            intent ?: return null
            if (resultCode != Activity.RESULT_OK) {
                return null
            }
            return getParams(intent)
        }

    }

}