package com.lollipop.base.listener

/**
 * @author lollipop
 * @date 4/16/21 22:06
 * 返回事件的监听器
 */
interface BackPressListener {

    /**
     * 当返回事件产生时，会触发此回调函数
     * 此时需要可以选择是否拦截事件
     * @return 如果返回为true，表示消费了此事件
     */
    fun onBackPressed(): Boolean

}