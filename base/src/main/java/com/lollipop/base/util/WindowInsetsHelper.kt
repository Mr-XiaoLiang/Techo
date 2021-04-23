package com.lollipop.base.util

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import java.lang.ref.WeakReference
import kotlin.math.max

/**
 * @author lollipop
 * @date 4/19/21 22:40
 * Window缩进的辅助工具
 */
class WindowInsetsHelper : View.OnLayoutChangeListener {

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
        target.addOnLayoutChangeListener(this)
    }

    /**
     * 解除绑定，释放View
     */
    fun unbind() {
        targetView.get()?.removeOnLayoutChangeListener(this)
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

    /**
     * 以快照的形式记录当前Margin值，并且以此为基础
     */
    fun snapshotMargin() {
        targetView.get()?.layoutParams?.let { layoutParams ->
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                baseMargin.set(
                        layoutParams.leftMargin,
                        layoutParams.topMargin,
                        layoutParams.rightMargin,
                        layoutParams.bottomMargin
                )
            }
        }
    }

    /**
     * 以快照的形式记录当前Padding值，并且以此为基础
     */
    fun snapshotPadding() {
        targetView.get()?.let { target ->
            basePadding.set(
                    target.paddingLeft,
                    target.paddingTop,
                    target.paddingRight,
                    target.paddingBottom
            )
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
                getDiff(left, top, right, bottom,
                        offsetByRoot, basePadding) { l, t, r, b ->
                    setPadding(target, l, t, r, b)
                }
            }
            OptionType.Margin -> {
                getDiff(left, top, right, bottom,
                        offsetByRoot, baseMargin) { l, t, r, b ->
                    setMargin(target, l, t, r, b)
                }
            }
        }
    }

    private fun getDiff(
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            offsetRect: Rect,
            minInsets: Rect,
            callback: (l: Int, t: Int, r: Int, b: Int) -> Unit) {
        callback(
                max(max(0, left - offsetRect.left), minInsets.left),
                max(max(0, top - offsetRect.top), minInsets.top),
                max(max(0, right - offsetRect.right), minInsets.right),
                max(max(0, bottom - offsetRect.bottom), minInsets.bottom)
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
        if (targetInViewPager) {
            findParentPager(target) { pager, current, self ->
                val pagerOffset = (current - self) * pager.width
                tempRectByLocation.offset(pagerOffset, 0)
            }
            findParentPager2(target) { pager, current, self ->
                val pagerOffsetX: Int
                val pagerOffsetY: Int
                if (pager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                    pagerOffsetX = (current - self) * pager.width
                    pagerOffsetY = 0
                } else {
                    pagerOffsetX = 0
                    pagerOffsetY = (current - self) * pager.height
                }
                tempRectByLocation.offset(pagerOffsetX, pagerOffsetY)
            }
        }
        return tempRectByLocation
    }

    private fun findParentPager(
            target: View,
            callback: (pager: ViewPager, current: Int, self: Int) -> Unit) {
        findParent<ViewPager>(target) { parent, child ->
            callback(parent, parent.currentItem, parent.indexOfChild(child))
        }
    }

    private fun findParentPager2(
            target: View,
            callback: (pager: ViewPager2, current: Int, self: Int) -> Unit) {
        findParent<ViewPager2>(target) { parent, child ->
            callback(parent, parent.currentItem, parent.indexOfChild(child))
        }
    }

    private inline fun <reified T : View> findParent(
            target: View, callback: (parent: T, child: View) -> Unit) {
        var self = target
        var parent = self.parent
        while (parent != null) {
            if (parent is T) {
                callback(parent, self)
                return
            }
            if (parent is View) {
                self = parent
                parent = self.parent
            } else {
                return
            }
        }
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

    override fun onLayoutChange(
            v: View?,
            left: Int, top: Int, right: Int, bottom: Int,
            oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {

        val pendingOption = pendingTask ?: return
        pendingTask = null
        pendingOption.rootGroup.get()?.let { rootGroup ->
            updateByType(
                    pendingOption.type,
                    rootGroup,
                    pendingOption.insets.left,
                    pendingOption.insets.top,
                    pendingOption.insets.right,
                    pendingOption.insets.bottom)
        }

    }

}
