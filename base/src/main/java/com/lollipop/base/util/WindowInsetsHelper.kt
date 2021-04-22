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

    private var pendingTask: Option? = null

    /**
     * targetView位于ViewPager中，
     * 计算时，会判定是否真的位于ViewPager，并且计算偏移量
     */
    var targetInViewPager = false

    /**
     * 绑定Target目标View
     * @param target 需要绑定的目标View
     */
    fun bind(target: View) {
        targetView = WeakReference<View>(target)
    }

    /**
     * 解除绑定，释放View
     */
    fun unbind() {
        targetView = WeakReference<View>(null)
    }

    /**
     * 更新padding信息，可能会覆盖现有的内容，会在快照基础上叠加
     * @param rootView 根结点的View
     * @param left 左侧缩进
     * @param top 顶部缩进
     * @param right 右侧缩进
     * @param bottom 底部缩进
     */
    fun updateByPadding(rootView: View, left: Int, top: Int, right: Int, bottom: Int) {
        updateByType(OptionType.Padding, rootView, left, top, right, bottom)
    }

    /**
     * 更新margin信息，可能会覆盖现有的内容，会在快照基础上叠加
     * @param rootView 根结点的View
     * @param left 左侧缩进
     * @param top 顶部缩进
     * @param right 右侧缩进
     * @param bottom 底部缩进
     */
    fun updateByMargin(rootView: View, left: Int, top: Int, right: Int, bottom: Int) {
        updateByType(OptionType.Margin, rootView, left, top, right, bottom)
    }

    /**
     * 直接为目标对象设置Margin
     * @param left 左侧缩进
     * @param top 顶部缩进
     * @param right 右侧缩进
     * @param bottom 底部缩进
     */
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

    /**
     * 直接为目标对象设置Padding
     * @param left 左侧缩进
     * @param top 顶部缩进
     * @param right 右侧缩进
     * @param bottom 底部缩进
     */
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
        val target = targetView.get() ?: return
        if (!target.isAttachedToWindow) {
            postOption(Option(
                    type,
                    WeakReference(rootView),
                    Rect(left, top, right, bottom)
            ))
            return
        }

        // TODO
    }

    private fun postOption(option: Option) {
        pendingTask = option
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

    /**
     * 操作类型
     */
    private enum class OptionType {
        /**
         * 以Padding的形式设置参数
         */
        Padding,

        /**
         * 以Margin的形式设置参数
         */
        Margin
    }

}
