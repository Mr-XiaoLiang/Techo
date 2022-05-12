package com.lollipop.techo.edit

import android.app.Activity
import com.lollipop.base.request.RequestLauncher

/**
 * @author lollipop
 * @date 2021/12/19 22:20
 */
interface PanelController {

    fun callClose(editDelegate: EditDelegate)

    val context: Activity

    val requestLauncher: RequestLauncher

}