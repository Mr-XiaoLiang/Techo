package com.lollipop.techo.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.lollipop.base.util.log
import kotlin.math.max
import kotlin.math.min

class PassiveScrollView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : ViewGroup(context, attributeSet, style) {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    private var maxContentHeight = 0

    private var onContentHeightChangedListener: OnContentHeightChangedListener? = null

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        log("onLayout")
        val offsetY = scrollY
        val maxWidth = width - paddingLeft - paddingRight
        val childTop = paddingTop - offsetY
        val childLeft = paddingLeft
        for (index in 0 until childCount) {
            getChildAt(index)?.let { child ->
                val childWidth = min(child.measuredWidth, maxWidth)
                val childHeight = child.measuredHeight
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
            }
        }
    }

//    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
//        super.onScrollChanged(l, t, oldl, oldt)
//        val offsetY = t - oldt
//        log("onScrollChanged: $offsetY")
//        for (index in 0 until childCount) {
//            getChildAt(index)?.offsetTopAndBottom(offsetY)
//        }
//    }

    fun offsetTo(offsetY: Int) {
        val newY = max(0, min(offsetY, maxContentHeight - height))
        val offset = newY - scrollY
        scrollY = newY
        for (index in 0 until childCount) {
            getChildAt(index)?.offsetTopAndBottom(offset)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var maxHeight = 0

        val childWidthSpec = MeasureSpec.makeMeasureSpec(
            widthSize - paddingLeft - paddingRight,
            MeasureSpec.AT_MOST
        )
        val childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

        for (index in 0 until childCount) {
            getChildAt(index)?.let { child ->
                child.measure(childWidthSpec, childHeightSpec)
                val measuredHeight = child.measuredHeight
                if (measuredHeight > maxHeight) {
                    maxHeight = measuredHeight
                }
            }
        }

        changeMaxOffset(max(0, maxHeight), heightSize)
        setMeasuredDimension(widthSize, heightSize)
    }

    fun setOnContentChangedListener(listener: OnContentHeightChangedListener) {
        this.onContentHeightChangedListener = listener
    }

    private fun changeMaxOffset(contentHeight: Int, height: Int) {
        if (maxContentHeight != contentHeight) {
            onContentHeightChangedListener?.onContentHeightChanged(contentHeight, height)
        }
        maxContentHeight = contentHeight
    }

    fun interface OnContentHeightChangedListener {
        fun onContentHeightChanged(contentHeight: Int, height: Int)
    }

}