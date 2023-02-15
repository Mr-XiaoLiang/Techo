package com.lollipop.vertical_page

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView

class VerticalPageScrollView(
    context: Context, attrs: AttributeSet?, style: Int
) : NestedScrollView(context, attrs, style) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val selfHeight = MeasureSpec.getSize(heightMeasureSpec)
        for (index in 0 until childCount) {
            getChildAt(index)?.let {
                if (it is VerticalPageLayout) {
                    it.setPageHeight(selfHeight)
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

}