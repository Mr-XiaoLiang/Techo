package com.lollipop.techo

import android.app.Activity
import android.app.Application
import com.lollipop.base.request.PermissionFlow
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentParse
import com.lollipop.techo.activity.HeaderActivity
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
                primary = getColor(R.color.pigmentPrimary),
                primaryVariant = getColor(R.color.pigmentPrimaryVariant),
                onPrimaryTitle = getColor(R.color.pigmentOnPrimaryTitle),
                onPrimaryBody = getColor(R.color.pigmentOnPrimaryBody),
                secondary = getColor(R.color.pigmentSecondary),
                secondaryVariant = getColor(R.color.pigmentSecondaryVariant),
                onSecondaryTitle = getColor(R.color.pigmentOnSecondaryTitle),
                onSecondaryBody = getColor(R.color.pigmentOnSecondaryBody)
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