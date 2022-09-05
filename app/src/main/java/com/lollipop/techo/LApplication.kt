package com.lollipop.techo

import android.app.Activity
import android.app.Application
import com.lollipop.base.request.PermissionFlow
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentParse
import com.lollipop.techo.util.AppUtil
import com.lollipop.techo.util.FontHelper
import com.lollipop.techo.view.PermissionFeedbackDialog

/**
 * @author lollipop
 * @date 2021/5/13 22:23
 */
class LApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        PermissionFlow.globalRationaleCallback = PermissionRationaleCallback()
        FontHelper.init(this)
        PigmentParse.init(
            Pigment(
                primary = getColor(R.color.brand_4),
                primaryVariant = getColor(R.color.brand_7),
                onPrimaryTitle = getColor(R.color.gray_1),
                onPrimaryBody = getColor(R.color.gray_2),
                secondary = getColor(R.color.brand_4),
                secondaryVariant = getColor(R.color.brand_7),
                onSecondaryTitle = getColor(R.color.gray_1),
                onSecondaryBody = getColor(R.color.gray_2)
            )
        )
        AppUtil.init(this)
    }

    class PermissionRationaleCallback : PermissionFlow.RationaleCallback {
        override fun showRationale(
            activity: Activity,
            permissions: String,
            feedback: PermissionFlow.PermissionFeedback
        ) {
            PermissionFeedbackDialog.showByPermission(activity, permissions, feedback)
        }
    }

}