package com.lollipop.bigboom

/**
 * @author lollipop
 * @date 2022/1/7 22:35
 * 接收爆炸碎片的接收器
 */
interface Basket {

    /**
     * 好的结果
     * @param value 被炸出来的碎片
     */
    fun goodPatches(value: Array<String>)

    /**
     * 没能成功引爆
     * @param code 错误码
     * @param msg 错误描述
     * @param throwable 发生的异常（如果有）
     */
    fun failedDetonate(code: Int, msg: String, throwable: Throwable?)

}