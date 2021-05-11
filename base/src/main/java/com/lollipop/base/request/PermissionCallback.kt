package com.lollipop.base.request

/**
 * @author lollipop
 * @date 2021/5/11 23:34
 * 请求包装的接口
 */
fun interface PermissionCallback {

    /**
     * 当请求权限返回的时候
     * @param permissions 请求的权限集合
     * @param grantResults 请求的结果
     */
    fun onPermissionsResult(result: PermissionResult)
}