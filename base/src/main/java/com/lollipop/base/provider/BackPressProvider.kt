package com.lollipop.base.provider

import com.lollipop.base.listener.BackPressListener

/**
 * @author lollipop
 * @date 4/16/21 22:04
 * 返回事件的提供者
 */
interface BackPressProvider {

    /**
     * 添加一个返回事件的监听器
     */
    fun addBackPressListener(listener: BackPressListener)

    /**
     * 移除一个返回事件的监听器
     */
    fun removeBackPressListener(listener: BackPressListener)

}