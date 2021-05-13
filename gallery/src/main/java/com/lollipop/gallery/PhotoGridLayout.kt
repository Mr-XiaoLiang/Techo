package com.lollipop.gallery

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * @author lollipop
 * @date 4/23/21 22:24
 * 照片的宫格排列Layout
 */
class PhotoGridLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    companion object {
        private const val SPAN_COUNT_SPARSE = 3
        private const val SPAN_COUNT_DENSE = 4
    }

    /**
     * 排版的样式
     */
    var layoutStyle = Style.Playbill

    /**
     * 间隔宽度
     */
    var spaceWidth = 0

    init {
        attrs?.let { attributeSet ->
            val typedValue = context.obtainStyledAttributes(attributeSet, R.styleable.PhotoGridLayout)
            spaceWidth = typedValue.getDimensionPixelSize(R.styleable.PhotoGridLayout_spaceWidth, 0)
            val styleValue = typedValue.getInt(R.styleable.PhotoGridLayout_layoutStyle, Style.Playbill.ordinal)
            layoutStyle = Style.values()[styleValue % Style.values().size]
            typedValue.recycle()
        }
    }

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
            Style.Playbill -> layoutByPlaybill(left, top, right, bottom)
            Style.Dense -> layoutByDense(left, top, right, bottom)
            Style.Sparse -> layoutBySparse(left, top, right, bottom)
        }
    }


    private fun measureBySparse(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureByGrid(SPAN_COUNT_SPARSE, widthMeasureSpec, heightMeasureSpec)
    }

    private fun measureByDense(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureByGrid(SPAN_COUNT_DENSE, widthMeasureSpec, heightMeasureSpec)
    }

    private fun measureByPlaybill(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val space = spaceWidth
        when (val count = childCount) {
            1 -> {
                measureByGrid(1, widthMeasureSpec, heightMeasureSpec)
            }
            2, 4 -> {
                measureByGrid(2, widthMeasureSpec, heightMeasureSpec)
            }
            3, 6 -> {
                val childWidthSmall = getChildWidthWithSpace(widthSize, space, 3)
                val childWidthBig = childWidthSmall * 2 + space
                for (index in 0 until count) {
                    val childWidth = if (index == 0) {
                        childWidthBig
                    } else {
                        childWidthSmall
                    }
                    val childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY)
                    getChildAt(index)?.measure(childWidthSpec, childWidthSpec)
                }
                val heightSize = if (count == 3) {
                    childWidthBig
                } else {
                    childWidthBig + childWidthSmall + space
                }
                setMeasuredDimension(
                        widthSize + paddingLeft + paddingRight,
                        heightSize + paddingTop + paddingBottom
                )
            }
            5, 7 -> {
                measureByTrapezoid(count / 2, widthMeasureSpec, heightMeasureSpec)
            }
            8 -> {
                val childWidthSmall = getChildWidthWithSpace(widthSize, space, 3)
                val childWidthMedium = getChildWidthWithSpace(widthSize, space, 2)
                val childWidthBig = childWidthSmall * 2 + space
                for (index in 0 until count) {
                    val childWidth = when {
                        index < 2 -> {
                            childWidthMedium
                        }
                        index == 2 -> {
                            childWidthBig
                        }
                        else -> {
                            childWidthSmall
                        }
                    }
                    val childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY)
                    getChildAt(index)?.measure(childWidthSpec, childWidthSpec)
                }
                val heightSize = childWidthSmall + childWidthMedium + childWidthBig + (space * 2)
                setMeasuredDimension(
                        widthSize + paddingLeft + paddingRight,
                        heightSize + paddingTop + paddingBottom
                )
            }
            else -> {
                measureBySparse(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    private fun measureByGrid(spanCount: Int, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val childWidth = getChildWidthWithSpace(widthSize, spaceWidth, spanCount)
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

    private fun measureByTrapezoid(spanCount: Int, widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val space = spaceWidth
        val count = childCount
        val childWidthBig = getChildWidthWithSpace(widthSize, space, spanCount)
        val childWidthSmall = getChildWidthWithSpace(widthSize, space, spanCount + 1)
        for (index in 0 until count) {
            val childWidth = if (index < spanCount) {
                childWidthBig
            } else {
                childWidthSmall
            }
            val childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY)
            getChildAt(index)?.measure(childWidthSpec, childWidthSpec)
        }
        val smallChildCount = count - spanCount
        val smallSpanCount = spanCount + 1
        var smallRowCount = 0
        if (smallChildCount > 0) {
            smallRowCount = smallChildCount / smallSpanCount
            if (smallChildCount % smallSpanCount != 0) {
                smallRowCount += 1
            }
        }
        val heightSize = childWidthBig + (smallRowCount * (childWidthSmall + space))
        setMeasuredDimension(
                widthSize + paddingLeft + paddingRight,
                heightSize + paddingTop + paddingBottom
        )
    }

    private fun layoutByDense(left: Int, top: Int, right: Int, bottom: Int) {
        layoutByGrid(SPAN_COUNT_DENSE, right - left)
    }

    private fun layoutBySparse(left: Int, top: Int, right: Int, bottom: Int) {
        layoutByGrid(SPAN_COUNT_SPARSE, right - left)
    }

    private fun layoutByPlaybill(left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val widthSize = width - paddingLeft - paddingRight
        val space = spaceWidth
        when (val count = childCount) {
            1 -> {
                layoutByGrid(1, width)
            }
            2, 4 -> {
                layoutByGrid(2, width)
            }
            3, 6 -> {
                val childWidthSmall = getChildWidthWithSpace(widthSize, space, 3)
                val childWidthBig = childWidthSmall * 2 + space
                for (index in 0 until count) {
                    var childLeft: Int
                    var childTop: Int
                    val childWidth: Int
                    when (index) {
                        0 -> {
                            childLeft = 0
                            childTop = 0
                            childWidth = childWidthBig
                        }
                        1, 2 -> {
                            childLeft = childWidthBig + space
                            childTop = (index - 1) * (childWidthSmall + space)
                            childWidth = childWidthSmall
                        }
                        else -> {
                            childLeft = (index - 3) % 3 * (childWidthSmall + space)
                            childTop = (index - 3) / 3 * (childWidthSmall + space) + childWidthBig + space
                            childWidth = childWidthSmall
                        }
                    }
                    childLeft += paddingLeft
                    childTop += paddingTop
                    getChildAt(index)?.layout(
                            childLeft,
                            childTop,
                            childLeft + childWidth,
                            childTop + childWidth
                    )
                }
            }
            5, 7 -> {
                layoutByTrapezoid(count / 2, width)
            }
            8 -> {
                val childWidthSmall = getChildWidthWithSpace(widthSize, space, 3)
                val childWidthMedium = getChildWidthWithSpace(widthSize, space, 2)
                val childWidthBig = childWidthSmall * 2 + space
                for (index in 0 until count) {
                    val childWidth: Int
                    var childLeft: Int
                    var childTop: Int
                    when {
                        index < 2 -> {
                            childWidth = childWidthMedium
                            childLeft = index % 2 * (childWidth + space)
                            childTop = 0
                        }
                        index == 2 -> {
                            childWidth = childWidthBig
                            childLeft = 0
                            childTop = childWidthMedium + space
                        }
                        index < 5 -> {
                            childWidth = childWidthSmall
                            childLeft = childWidthBig + space
                            childTop = (index - 3) * (childWidth + space) + childWidthMedium + space
                        }
                        else -> {
                            val smallIndex = index - 5
                            childWidth = childWidthSmall
                            childLeft = smallIndex % 3 * (childWidth + space)
                            childTop = (smallIndex / 3 * (childWidth + space)
                                    + childWidthMedium + childWidthBig + (space * 2))
                        }
                    }
                    childLeft += paddingLeft
                    childTop += paddingTop
                    getChildAt(index)?.layout(
                            childLeft,
                            childTop,
                            childLeft + childWidth,
                            childTop + childWidth
                    )
                }
            }
            else -> {
                layoutBySparse(left, top, right, bottom)
            }
        }
    }

    private fun layoutByGrid(spanCount: Int, width: Int) {
        val widthSize = width - paddingLeft - paddingRight
        val childWidth = getChildWidthWithSpace(widthSize, spaceWidth, spanCount)
        val count = childCount
        val space = spaceWidth
        for (index in 0 until count) {
            val childLeft = index % spanCount * (childWidth + space) + paddingLeft
            val childTop = index / spanCount * (childWidth + space) + paddingTop
            getChildAt(index)?.layout(
                    childLeft, childTop, childLeft + childWidth, childTop + childWidth)
        }
    }

    private fun layoutByTrapezoid(spanCount: Int, width: Int) {
        val widthSize = width - paddingLeft - paddingRight
        val space = spaceWidth
        val count = childCount
        val smallSpanCount = spanCount + 1
        val childWidthBig = getChildWidthWithSpace(widthSize, space, spanCount)
        val childWidthSmall = getChildWidthWithSpace(widthSize, space, smallSpanCount)
        for (index in 0 until count) {
            val childWidth: Int
            var childLeft: Int
            var childTop: Int
            if (index < spanCount) {
                childWidth = childWidthBig
                childLeft = index % spanCount * (childWidth + space)
                childTop = index / spanCount * (childWidth + space)
            } else {
                childWidth = childWidthSmall
                val childIndex = index - spanCount
                childLeft = childIndex % smallSpanCount * (childWidth + space)
                childTop = childIndex / smallSpanCount * (childWidth + space) + childWidthBig + space
            }
            childLeft += paddingLeft
            childTop += paddingTop
            getChildAt(index)?.layout(
                    childLeft,
                    childTop,
                    childLeft + childWidth,
                    childTop + childWidth
            )
        }
    }

    private fun getChildWidthWithSpace(widthSize: Int, space: Int, spanCount: Int): Int {
        return (widthSize - (space * (spanCount - 1))) / spanCount
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