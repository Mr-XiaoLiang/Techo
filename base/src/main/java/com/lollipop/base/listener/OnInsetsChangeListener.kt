package com.lollipop.base.listener

import android.view.View

/**
 * @author lollipop
 * @date 4/18/21 19:56
 * 页面窗口缩进的监听器
 */
interface OnInsetsChangeListener {

    /**
     * 当内容缩进变化时触发当回调函数
     * @param root 根结点
     * @param left 左侧缩进尺寸
     * @param top 顶部缩进尺寸
     * @param right 右侧缩进尺寸
     * @param bottom 底部缩进尺寸
     */
    fun onInsetsChanged(root: View, left: Int, top: Int, right: Int, bottom: Int)

}