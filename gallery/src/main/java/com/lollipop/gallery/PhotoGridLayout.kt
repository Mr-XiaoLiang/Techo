package com.lollipop.gallery

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView

/**
 * @author lollipop
 * @date 4/23/21 22:24
 * 照片的宫格排列Layout
 */
class PhotoGridLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        CardView(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context): this(context, null)

    companion object {
        private const val SPAN_COUNT_SPARSE = 3
        private const val SPAN_COUNT_DENSE = 5
    }

    /**
     * 排版的样式
     */
    var layoutStyle = Style.Playbill

    /**
     * 间隔宽度
     */
    var spaceWidth = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (childCount == 0 || MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        when (layoutStyle) {
            Style.Playbill -> measureByPlaybill(widthMeasureSpec, heightMeasureSpec)
            Style.Dense -> measureByDense(widthMeasureSpec, heightMeasureSpec)
            Style.Sparse -> measureBySparse(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (childCount == 0) {
            super.onLayout(changed, left, top, right, bottom)
            return
        }
        when (layoutStyle) {
            Style.Playbill -> layoutByPlaybill(changed, left, top, right, bottom)
            Style.Dense -> layoutByDense(changed, left, top, right, bottom)
            Style.Sparse -> layoutBySparse(changed, left, top, right, bottom)
        }
    }


    private fun measureBySparse(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureByGrid(SPAN_COUNT_SPARSE, widthMeasureSpec, heightMeasureSpec)
    }

    private fun measureByDense(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureByGrid(SPAN_COUNT_DENSE, widthMeasureSpec, heightMeasureSpec)
    }

    private fun measureByPlaybill(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // TODO
    }

    private fun measureByGrid(spanCount: Int, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val childWidth = (widthSize - (spaceWidth * (spanCount - 1))) / spanCount
        val count = childCount
        val rowCount = if (count % spanCount != 0) {
            count / spanCount + 1
        } else {
            count / spanCount
        }
        val heightSize = rowCount * (childWidth + spaceWidth) - spaceWidth
        val childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY)
        for (index in 0 until count) {
            getChildAt(index)?.measure(childWidthSpec, childWidthSpec)
        }
        setMeasuredDimension(
                widthSize + paddingLeft + paddingRight,
                heightSize + paddingTop + paddingBottom
        )
    }


    private fun layoutByDense(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutByGrid(SPAN_COUNT_DENSE, changed, left, top, right, bottom)
    }

    private fun layoutBySparse(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutByGrid(SPAN_COUNT_SPARSE, changed, left, top, right, bottom)
    }

    private fun layoutByPlaybill(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // TODO
    }

    private fun layoutByGrid(
            spanCount: Int,
            changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val widthSize = width - paddingLeft - paddingRight
        val childWidth = (widthSize - (spaceWidth * (spanCount - 1))) / spanCount
        val count = childCount
        val space = spaceWidth
        for (index in 0 until count) {
            val childLeft = index % spanCount * (childWidth + space)
            val childTop = index / spanCount * (childWidth + space)
            getChildAt(index)?.layout(
                    childLeft, childTop, childLeft + childWidth, childTop + childWidth)
        }
    }

    enum class Style {
        /**
         * 稀疏的排列方式
         * 3列的模式
         */
        Sparse,

        /**
         * 密集的排列方式
         * 4列的模式
         */
        Dense,

        /**
         * 海报排列
         * 不同数量的图片有不同的排列方式
         */
        Playbill
    }

}