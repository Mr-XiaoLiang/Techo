package com.lollipop.punch2.view

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView

class LimitHorizontalScrollView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : HorizontalScrollView(context, attributeSet) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        getChildAt(0)?.let {
            if (it is LimitChildView) {
                it.setGroupMaxWidth(widthSize)
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    interface LimitChildView {
        fun setGroupMaxWidth(width: Int)
    }

}