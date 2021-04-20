package com.lollipop.base.util

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
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

    fun updateByPadding(rootView: View, left: Int, top: Int, right: Int, bottom: Int) {
        updateByType(OptionType.Padding, rootView, left, top, right, bottom)
    }

    fun updateByMargin(rootView: View, left: Int, top: Int, right: Int, bottom: Int) {
        updateByType(OptionType.Margin, rootView, left, top, right, bottom)
    }

    fun setMargin(left: Int, top: Int, right: Int, bottom: Int) {
        targetView.get()?.let { target ->
            target.layoutParams?.let { layoutParams ->
                if (layoutParams is ViewGroup.MarginLayoutParams) {
                    layoutParams.setMargins(left, top, right, bottom)
                }
                target.layoutParams = layoutParams
            }
        }
    }

    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        targetView.get()?.setPadding(left, top, right, bottom)
    }

    private fun updateByType(
            type: OptionType,
            rootView: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int) {
        val target = targetView.get()?:return
        if (!target.isAttachedToWindow) {
            return
        }

        // TODO
    }

    private fun postOption(option: Option) {
        // TODO
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