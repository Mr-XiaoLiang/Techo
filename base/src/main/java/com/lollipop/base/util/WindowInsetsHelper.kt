package com.lollipop.base.util

import android.graphics.Rect
import android.view.View
import java.lang.ref.WeakReference

/**
 * @author lollipop
 * @date 4/19/21 22:40
 * Window缩进的辅助工具
 */
class WindowInsetsHelper {

    private var targetView = WeakReference<View>(null)

    fun bind(target: View) {
        targetView = WeakReference<View>(target)
    }

    /**
     * 参数项
     * 用于确定一次操作
     */
    private class Option(
            /** 操作类型 **/
            val type: OptionType,
            /** 根结点的View **/
            val rootGroup: WeakReference<View>,
            /** 缩进大小 **/
            val insets: Rect,
    )

    private enum class OptionType {
        Padding, Margin
    }

}