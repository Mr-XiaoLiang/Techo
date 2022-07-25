package com.lollipop.techo.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import kotlin.math.min

class VerticalPageLayout(
    context: Context, attrs: AttributeSet?, style: Int
) : ViewGroup(context, attrs, style) {

    private var pageHeight = 0

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var maxHeight = 0
        val childWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
        val childHeightSpec = MeasureSpec.makeMeasureSpec(
            if (pageHeight > 0) {
                pageHeight
            } else {
                heightSize
            },
            MeasureSpec.AT_MOST
        )
        val matchHeightSpec = MeasureSpec.makeMeasureSpec(
            if (pageHeight > 0) {
                pageHeight
            } else {
                heightSize
            },
            MeasureSpec.EXACTLY
        )
        for (index in 0 until childCount) {
            getChildAt(index)?.let { child ->
                if (child.layoutParams.height == LayoutParams.MATCH_PARENT) {
                    child.measure(childWidthSpec, matchHeightSpec)
                } else {
                    child.measure(childWidthSpec, childHeightSpec)
                }
                maxHeight += if (pageHeight > 0) {
                    min(child.measuredHeight, pageHeight)
                } else {
                    child.measuredHeight
                }
            }
        }
        maxHeight += paddingTop
        maxHeight += paddingBottom
        setMeasuredDimension(widthSize, maxHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var top = paddingTop
        val left = paddingLeft
        val maxWidth = width - paddingLeft - paddingRight
        val maxHeight = if (pageHeight > 0) {
            pageHeight
        } else {
            height
        }
        for (index in 0 until childCount) {
            getChildAt(index)?.let { child ->
                val childWidth = min(child.measuredWidth, maxWidth)
                val childHeight = min(child.measuredHeight, maxHeight)
                child.layout(left, top, left + childWidth, top + childHeight)
                top += childHeight
            }
        }
    }

    fun setPageHeight(height: Int) {
        pageHeight = height
        requestLayout()
    }

}