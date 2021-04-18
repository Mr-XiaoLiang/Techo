package com.lollipop.base.provider

import com.lollipop.base.listener.OnInsetsChangeListener

/**
 * @author lollipop
 * @date 4/18/21 20:00
 * 窗口缩进信息的提供者
 */
interface WindowInsetsProvider {

    /**
     * 添加一个缩进监听器
     */
    fun addInsetsChangeListener(listener: OnInsetsChangeListener)

    /**
     * 移除一个缩进监听器
     */
    fun removeInsetsChangeListener(listener: OnInsetsChangeListener)

}