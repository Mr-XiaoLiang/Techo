package com.lollipop.punch2.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlin.math.max
import kotlin.math.min

class LimitWidthLinearLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : LinearLayout(context, attributeSet), LimitHorizontalScrollView.LimitChildView {

    companion object {
        /**
         * 高度按照最大的child
         */
        const val HEIGHT_BY_MAX = -3

        /**
         * 高度按照第一个child
         */
        const val HEIGHT_BY_FIRST = 0

        /**
         * 高度按照最后一个child
         */
        const val HEIGHT_BY_LAST = -1

        /**
         * 高度按照第一个自适应高度的child
         */
        const val HEIGHT_BY_FIRST_WRAP = -2
    }

    var heightByChild = HEIGHT_BY_FIRST_WRAP

    private var parentWidth = 0

    init {
        orientation = HORIZONTAL
    }

    override fun setOrientation(orientation: Int) {
        // 固定方向，我们只能使用横向排列
        super.setOrientation(HORIZONTAL)
    }

    override fun setGroupMaxWidth(width: Int) {
        parentWidth = width
    }

    private fun getChildMaxWidth(childLayoutParams: LayoutParams): Int {
        return (parentWidth
                - paddingLeft
                - paddingRight
                - childLayoutParams.leftMargin
                - childLayoutParams.rightMargin)
    }

    private fun getChileMaxHeight(childLayoutParams: LayoutParams, srcHeightSize: Int): Int {
        return (srcHeightSize
                - paddingTop
                - paddingBottom
                - childLayoutParams.topMargin
                - childLayoutParams.bottomMargin)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (parentWidth == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        var widthSize = paddingLeft + paddingRight

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val srcHeightSize = max(
            0,
            MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom
        )
        var heightSize = 0
        var childState = 0

        var heightByChildEnable = false
        val hbc = heightByChild
        val heightChildIndex = when {
            hbc in HEIGHT_BY_FIRST..<childCount -> {
                hbc
            }

            heightByChild == HEIGHT_BY_LAST -> {
                childCount - 1
            }

            heightByChild == HEIGHT_BY_FIRST_WRAP -> {
                findFirstWrapHeightChild()
            }

            else -> {
                -1
            }
        }
        if (heightChildIndex >= 0) {
            val child = getChildAt(heightChildIndex)
            if (child != null && child.visibility != View.GONE) {
                heightByChildEnable = true
                measureChild(child, true, srcHeightSize, heightSize)
                if (heightMode != MeasureSpec.EXACTLY) {
                    heightSize = child.measuredHeight
                    if (heightMode == MeasureSpec.AT_MOST) {
                        heightSize = min(srcHeightSize, heightSize)
                    }
                }
                childState = combineMeasuredStates(childState, child.measuredState)
                val childLayoutParams = getChildLayoutParams(child)
                widthSize += childLayoutParams.leftMargin
                widthSize += childLayoutParams.rightMargin
                widthSize += child.measuredWidth
            }
        }

        for (index in 0 until childCount) {
            if (index == heightChildIndex) {
                continue
            }
            val measureHeight = !heightByChildEnable
            val child = getChildAt(index) ?: continue
            if (child.visibility == View.GONE) continue

            val childLayoutParams = getChildLayoutParams(child)
            measureChild(child, measureHeight, srcHeightSize, heightSize)

            childState = combineMeasuredStates(childState, child.measuredState)
            if (measureHeight && heightMode != MeasureSpec.EXACTLY) {
                heightSize = max(child.measuredHeight, heightSize)
                if (heightMode == MeasureSpec.AT_MOST) {
                    heightSize = min(srcHeightSize, heightSize)
                }
            }
            widthSize += childLayoutParams.leftMargin
            widthSize += childLayoutParams.rightMargin
            widthSize += child.measuredWidth
        }
        setMeasuredDimension(
            resolveSizeAndState(widthSize, widthMeasureSpec, childState),
            resolveSizeAndState(
                heightSize + paddingTop + paddingBottom,
                heightMeasureSpec,
                childState
            )
        )
    }

    private fun findFirstWrapHeightChild(): Int {
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view != null && view.visibility != View.GONE) {
                val layoutParams = getChildLayoutParams(view)
                if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    return i
                }
            }
        }
        return -1
    }

    private fun measureChild(
        child: View,
        measureHeight: Boolean,
        srcHeightSize: Int,
        heightSize: Int
    ) {
        val childLayoutParams = getChildLayoutParams(child)
        val childLp = child.layoutParams
        val childWidthMeasureSpec = if (childLp.width == LayoutParams.MATCH_PARENT) {
            MeasureSpec.makeMeasureSpec(
                getChildMaxWidth(childLayoutParams),
                MeasureSpec.EXACTLY
            )
        } else if (childLp.width > 0) {
            MeasureSpec.makeMeasureSpec(
                min(childLp.width, getChildMaxWidth(childLayoutParams)),
                MeasureSpec.EXACTLY
            )
        } else {
            MeasureSpec.makeMeasureSpec(
                getChildMaxWidth(childLayoutParams),
                MeasureSpec.AT_MOST
            )
        }
        val childMaxHeight = if (measureHeight) {
            getChileMaxHeight(childLayoutParams, srcHeightSize)
        } else {
            heightSize
        }
        val childHeightMeasureSpec = if (childLp.height > 0) {
            MeasureSpec.makeMeasureSpec(
                min(childMaxHeight, childLp.height),
                MeasureSpec.AT_MOST
            )
        } else {
            MeasureSpec.makeMeasureSpec(
                childMaxHeight,
                if (childLp.height == LayoutParams.MATCH_PARENT) {
                    MeasureSpec.EXACTLY
                } else {
                    MeasureSpec.AT_MOST
                }
            )
        }
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var childLeft = paddingLeft
        val groupMaxHeight = height - paddingTop - paddingBottom
        val groupMaxWidth = min(width, parentWidth) - paddingLeft - paddingRight
        for (index in 0 until childCount) {
            val child = getChildAt(index) ?: continue

            if (!child.isShown) {
                child.layout(0, 0, 0, 0)
                continue
            }

            val childLayoutParams = getChildLayoutParams(child)
            val childTop = paddingTop + childLayoutParams.topMargin
            childLeft += childLayoutParams.leftMargin
            val childHeight = min(
                child.measuredHeight,
                (groupMaxHeight - childLayoutParams.topMargin - childLayoutParams.bottomMargin)
            )
            val childWidth = min(
                child.measuredWidth,
                groupMaxWidth - childLayoutParams.leftMargin - childLayoutParams.rightMargin
            )
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
            childLeft += childWidth
            childLeft += childLayoutParams.rightMargin
        }
    }

    private fun getChildLayoutParams(child: View): LayoutParams {
        var childLayoutParams = child.layoutParams ?: generateDefaultLayoutParams()
        if (childLayoutParams !is LayoutParams) {
            childLayoutParams = generateLayoutParams(childLayoutParams)
        }
        childLayoutParams as LayoutParams
        return childLayoutParams
    }

    fun findOffsetByIndex(index: Int): Int {
        if (index < 0 || index >= childCount) {
            return 0
        }
        val child = getChildAt(index) ?: return 0
        return findOffset(child)
    }

    fun findOffsetById(id: Int): Int {
        val child = findViewById<View>(id) ?: return 0
        return findOffset(child)
    }

    fun findOffset(view: View): Int {
        if (view.parent != this) {
            return 0
        }
        return view.left
    }

}