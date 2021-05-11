package com.lollipop.base.request

import android.content.Intent

/**
 * @author lollipop
 * @date 2021/5/12 00:48
 * 发起请求的启动器
 */
interface RequestLauncher {

    /**
     * 请求辅助器的实现类
     */
    val requestHelper: RequestHelper

    /**
     * 发起intent请求
     * 主要用于startActivity
     */
    fun requestActivity(intent: Intent, callback: RequestCallback) {
        requestHelper.requestActivity(intent, callback)
    }

    /**
     * 请求权限
     */
    fun requestPermission(permission: Array<String>, callback: PermissionCallback) {
        requestHelper.requestPermission(permission, callback)
    }

}