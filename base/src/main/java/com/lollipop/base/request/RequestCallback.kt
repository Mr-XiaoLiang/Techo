package com.lollipop.base.request

/**
 * @author lollipop
 * @date 2021/5/11 23:34
 * 请求包装的接口
 */
fun interface RequestCallback {

    /**
     * 当activity返回的时候
     * @param result 请求结果返回的包装体
     */
    fun onActivityResult(result: RequestResult)
}