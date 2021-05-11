package com.lollipop.base.request

import android.app.Activity
import android.content.Intent

/**
 * @author lollipop
 * @date 2021/5/12 00:33
 * 请求结果的返回值
 */
class RequestResult(
    val resultCode: Int,
    val data: Intent?
) {

    val isOk: Boolean = resultCode == Activity.RESULT_OK

    val isCanceled: Boolean = resultCode == Activity.RESULT_CANCELED



}