package com.lollipop.techo.edit

import androidx.appcompat.app.AppCompatActivity
import com.lollipop.techo.edit.base.EditDelegate
import com.lollipop.techo.util.permission.PermissionLauncher

/**
 * @author lollipop
 * @date 2021/12/19 22:20
 */
interface PanelController {

    fun callClose(editDelegate: EditDelegate<*>)

    val context: AppCompatActivity

    fun findLauncher(who: Any, permission: Array<String>): PermissionLauncher?

}