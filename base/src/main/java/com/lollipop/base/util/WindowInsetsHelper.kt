package com.lollipop.base.util

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import java.lang.ref.WeakReference
import kotlin.math.max

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
     * 一个被复用的尺寸结构体
     * 主要用于计算位置的偏差
     */
    private val tempRectByLocation = Rect()

    /**
     * 基础的Margin值，它表示了最小的margin
     * 如果缩进尺寸小于此值，那么会保持在此值
     */
    private val baseMargin = Rect()

    /**
     * 基础的padding值，它代表了最小的padding值
     * 如果缩进尺寸小于此值，那么将会保持在此值
     */
    private val basePadding = Rect()

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
            setMargin(target, left, top, right, bottom)
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
        targetView.get()?.let { target ->
            setPadding(target, left, top, right, bottom)
        }
    }

    private fun setMargin(target: View, left: Int, top: Int, right: Int, bottom: Int) {
        target.layoutParams?.let { layoutParams ->
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.setMargins(left, top, right, bottom)
            }
            target.layoutParams = layoutParams
        }
    }

    private fun setPadding(target: View, left: Int, top: Int, right: Int, bottom: Int) {
        target.setPadding(left, top, right, bottom)
    }

    private fun updateByType(
            type: OptionType,
            rootView: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int) {
        val target = targetView.get() ?: return
        if (!target.isAttachedToWindow || target.width < 1 || target.height < 1) {
            postOption(Option(
                    type,
                    WeakReference(rootView),
                    Rect(left, top, right, bottom)
            ))
            return
        }
        // 如果不是自己的父节点，那么放弃事件，因为他的事件毫无意义
        if (!checkRoot(rootView, target)) {
            return
        }
        val offsetByRoot = findOffsetByRoot(rootView, target)
        when (type) {
            OptionType.Padding -> {
                getDiff(offsetByRoot, basePadding) { l, t, r, b ->
                    setPadding(target, l, t, r, b)
                }
            }
            OptionType.Margin -> {
                getDiff(offsetByRoot, baseMargin) { l, t, r, b ->
                    setMargin(target, l, t, r, b)
                }
            }
        }
    }

    private fun getDiff(targetInsets: Rect, minInsets: Rect,
                        callback: (l: Int, t: Int, r: Int, b: Int) -> Unit) {
        callback(
                max(targetInsets.left, minInsets.left),
                max(targetInsets.top, minInsets.top),
                max(targetInsets.right, minInsets.right),
                max(targetInsets.bottom, minInsets.bottom)
        )
    }

    private fun findOffsetByRoot(rootView: View, target: View): Rect {
        val rootLocation = rootView.getLocationInWindow()
        val targetLocation = target.getLocationInWindow()
        val offsetLeft = targetLocation[0] - rootLocation[0]
        val offsetTop = targetLocation[1] - rootLocation[1]
        val offsetRight = rootLocation[0] + rootView.width - targetLocation[0] - target.width
        val offsetBottom = rootLocation[1] + rootView.height - targetLocation[1] - target.height
        tempRectByLocation.set(offsetLeft, offsetTop, offsetRight, offsetBottom)
        return tempRectByLocation
    }

    private fun View.getLocationInWindow(): IntArray {
        val location = IntArray(2)
        getLocationInWindow(location)
        return location
    }

    private fun checkRoot(rootView: View, target: View): Boolean {
        var parent: ViewParent? = target.parent
        while (parent != null) {
            if (parent == rootView) {
                return true
            }
            parent = parent.parent
        }
        return false
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
