package com.lollipop.base.request

import android.content.pm.PackageManager

/**
 * @author lollipop
 * @date 2021/5/12 00:22
 * 权限请求的结果集合
 */
class PermissionResult(
    val permissions: Array<out String>,
    val grantResults: IntArray
) {

    companion object {
        const val GRANTED = PackageManager.PERMISSION_GRANTED
        const val DENIED = PackageManager.PERMISSION_DENIED
    }

    fun isGranted(permission: String): Boolean {
        val index = permissions.indexOf(permission)
        if (index < 0) {
            return false
        }
        return grantResults[index] == GRANTED
    }

}