package com.lollipop.techo.view

import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.lollipop.base.util.dp2px
import com.lollipop.techo.data.SplitStyle
import com.lollipop.techo.util.SplitLoader
import com.lollipop.techo.view.SplitView.WidthType.ABSOLUTELY
import com.lollipop.techo.view.SplitView.WidthType.MATCH

class SplitView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : FrameLayout(context, attributeSet, style) {

    companion object {
        private const val DEFAULT_SPLIT_HEIGHT = 5
        private const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
    }

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    private val imageView = ImageView(context)

    private val tempPoint = Point()

    private val defaultSplitHeight: Int
        get() {
            return DEFAULT_SPLIT_HEIGHT.dp2px
        }

    init {
        addView(imageView)
        if (isInEditMode) {
            load(SplitStyle.Default)
        }
    }

    fun load(splitStyle: SplitStyle) {
        val info = SplitLoader.getInfo(splitStyle)
        imageView.setImageDrawable(getDrawable(info))
        updateSize(info)
    }

    private fun updateSize(info: SplitLoader.SplitInfo) {
        imageView.layoutParams = RatioLayoutParams(
            info.width.dp2px,
            info.height.dp2px,
            info.ratio,
            info.widthType,
            info.heightType
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var maxChildHeight = 0
        val maxChildWidth = widthSize - paddingLeft - paddingRight
        for (i in 0 until childCount) {
            getChildAt(i)?.let { child ->
                val layoutParams = formatLayoutParams(child.layoutParams)
                getChildSize(tempPoint, maxChildWidth, layoutParams)
                child.measure(
                    MeasureSpec.makeMeasureSpec(tempPoint.x, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(tempPoint.y, MeasureSpec.EXACTLY),
                )
                if (maxChildHeight < tempPoint.y) {
                    maxChildHeight = tempPoint.y
                }
            }
        }
        maxChildHeight += paddingTop + paddingBottom
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = maxChildHeight
        } else if (heightMode == MeasureSpec.AT_MOST && heightSize > maxChildHeight) {
            heightSize = maxChildHeight
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val maxChildWidth = width - paddingLeft - paddingRight
        val maxChildHeight = height - paddingTop - paddingBottom
        for (i in 0 until childCount) {
            getChildAt(i)?.let { child ->
                val layoutParams = formatLayoutParams(child.layoutParams)
                getChildSize(tempPoint, maxChildWidth, layoutParams)
                val childLeft = ((maxChildWidth - tempPoint.x) / 2) + paddingLeft
                val childTop = ((maxChildHeight - tempPoint.y) / 2) + paddingTop
                child.layout(childLeft, childTop, childLeft + tempPoint.x, childTop + tempPoint.y)
            }
        }
    }

    private fun getChildSize(outSize: Point, maxChildWidth: Int, layoutParams: RatioLayoutParams) {
        val childWidth = when (layoutParams.widthType) {
            MATCH -> {
                maxChildWidth
            }
            ABSOLUTELY -> {
                layoutParams.width
            }
        }
        when (layoutParams.heightType) {
            HeightType.ABSOLUTELY -> {
                outSize.set(childWidth, layoutParams.height)
            }
            HeightType.RATIO -> {
                val ratio = layoutParams.ratio
                val childHeight = if (ratio.isNaN()) {
                    defaultSplitHeight
                } else {
                    (childWidth / ratio).toInt()
                }
                outSize.set(childWidth, childHeight)
            }
        }
    }

    private fun formatLayoutParams(lp: ViewGroup.LayoutParams?): RatioLayoutParams {
        if (lp == null) {
            return RatioLayoutParams(MATCH_PARENT, 5.dp2px)
        }
        if (lp is RatioLayoutParams) {
            return lp
        }
        return RatioLayoutParams(lp)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is RatioLayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return RatioLayoutParams(MATCH_PARENT, 5.dp2px)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return RatioLayoutParams(super.generateLayoutParams(attrs))
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        lp ?: return generateDefaultLayoutParams()
        return RatioLayoutParams(lp)
    }

    class RatioLayoutParams : LayoutParams {
        var ratio = Float.NaN

        var widthType = MATCH
        var heightType = HeightType.ABSOLUTELY

        constructor(width: Int, height: Int) : super(width, height)

        constructor(
            width: Int,
            height: Int,
            ratio: Float,
            widthType: WidthType,
            heightType: HeightType
        ) : super(width, height) {
            this.ratio = ratio
            this.widthType = widthType
            this.heightType = heightType
        }

        constructor(source: ViewGroup.LayoutParams) : super(source) {
            if (source is RatioLayoutParams) {
                this.ratio = source.ratio
                this.widthType = source.widthType
                this.heightType = source.heightType
            }
        }
    }

    private fun getDrawable(info: SplitLoader.SplitInfo): Drawable? {
        return try {
            ResourcesCompat.getDrawable(resources, info.resourceId, context.theme)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    enum class WidthType {
        MATCH, ABSOLUTELY
    }

    enum class HeightType {
        ABSOLUTELY, RATIO
    }

}