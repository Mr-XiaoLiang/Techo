package com.lollipop.techo.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.lollipop.base.util.ListenerManager
import com.lollipop.base.util.lazyLogD
import kotlin.math.max

class PassiveScrollView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : FrameLayout(context, attributeSet, style) {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    private var maxContentHeight = 0

    private val onContentHeightChangedListener = ListenerManager<OnContentHeightChangedListener>()

    private val log by lazyLogD()

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        var maxHeight = 0
        for (index in 0 until childCount) {
            getChildAt(index)?.let { child ->
                val childHeight = child.height
                if (maxHeight < childHeight) {
                    maxHeight = childHeight
                    log("${child::class.java.simpleName}: $childHeight")
                }
            }
        }
        changeMaxOffset(max(0, maxHeight + paddingTop + paddingBottom), height)
    }

    override fun measureChild(
        child: View,
        parentWidthMeasureSpec: Int,
        parentHeightMeasureSpec: Int
    ) {
        val lp = child.layoutParams
        val childWidthMeasureSpec = getChildMeasureSpec(
            parentWidthMeasureSpec,
            paddingLeft + paddingRight, lp.width
        )
        val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun measureChildWithMargins(
        child: View,
        parentWidthMeasureSpec: Int, widthUsed: Int,
        parentHeightMeasureSpec: Int, heightUsed: Int
    ) {
        val lp = child.layoutParams as MarginLayoutParams
        val childWidthMeasureSpec = getChildMeasureSpec(
            parentWidthMeasureSpec,
            (paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin + widthUsed),
            lp.width
        )
        val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
            lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED
        )
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    fun addListener(listener: OnContentHeightChangedListener) {
        onContentHeightChangedListener.addListener(listener)
    }

    fun removeListener(listener: OnContentHeightChangedListener) {
        onContentHeightChangedListener.removeListener(listener)
    }

    private fun changeMaxOffset(contentHeight: Int, height: Int) {
        log("changeMaxOffset: $contentHeight")
        if (maxContentHeight != contentHeight) {
            onContentHeightChangedListener.invoke {
                it.onContentHeightChanged(
                    contentHeight,
                    height
                )
            }
        }
        maxContentHeight = contentHeight
    }

    fun interface OnContentHeightChangedListener {
        fun onContentHeightChanged(contentHeight: Int, height: Int)
    }

}