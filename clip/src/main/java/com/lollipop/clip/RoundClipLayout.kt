package com.lollipop.clip

import android.content.Context
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.withStyledAttributes

class RoundClipLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ClipLayout(context, attrs) {

    private val radius = FloatArray(8)

    init {
        context.withStyledAttributes(attrs, R.styleable.RoundClipLayout) {
            val defRadius = getDimensionPixelSize(
                R.styleable.RoundClipLayout_android_radius, 0
            )
            val leftTop = getDimensionPixelSize(
                R.styleable.RoundClipLayout_leftTop, defRadius
            ).toFloat()
            val rightTop = getDimensionPixelSize(
                R.styleable.RoundClipLayout_rightTop, defRadius
            ).toFloat()
            val rightBottom = getDimensionPixelSize(
                R.styleable.RoundClipLayout_rightBottom, defRadius
            ).toFloat()
            val leftBottom = getDimensionPixelSize(
                R.styleable.RoundClipLayout_leftBottom, defRadius
            ).toFloat()
            setRadius(leftTop, rightTop, rightBottom, leftBottom)
        }
    }

    fun setRadius(
        leftTopX: Float,
        leftTopY: Float,
        rightTopX: Float,
        rightTopY: Float,
        rightBottomX: Float,
        rightBottomY: Float,
        leftBottomX: Float,
        leftBottomY: Float
    ) {
        radius[0] = leftTopX
        radius[1] = leftTopY

        radius[2] = rightTopX
        radius[3] = rightTopY

        radius[4] = rightBottomX
        radius[5] = rightBottomY

        radius[6] = leftBottomX
        radius[7] = leftBottomY
        onRadiusChanged()
    }

    fun setRadius(
        leftTop: Float,
        rightTop: Float,
        rightBottom: Float,
        leftBottom: Float,
    ) {
        setRadius(
            leftTop, leftTop,
            rightTop, rightTop,
            rightBottom, rightBottom,
            leftBottom, leftBottom
        )
    }

    fun setRadiusDp(
        leftTop: Float,
        rightTop: Float,
        rightBottom: Float,
        leftBottom: Float,
    ) {
        setRadius(
            getDp(leftTop),
            getDp(rightTop),
            getDp(rightBottom),
            getDp(leftBottom),
        )
    }

    fun setRadius(r: Float) {
        setRadius(r, r, r, r)
    }

    fun setRadiusDp(r: Float) {
        setRadius(getDp(r))
    }

    private fun getDp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }

    private fun onRadiusChanged() {
        rebuildDefaultPath()
        rebuildStrokePath()
    }

    override fun rebuildClipPathWithStroke(path: Path) {
        if (width < 1 || height < 1) {
            return
        }
        val halfStroke = strokeWidth * 0.5F
        val radii = FloatArray(radius.size)
        for (i in radius.indices) {
            radii[i] = radius[i] - halfStroke
        }
        path.addRoundRect(
            halfStroke,
            halfStroke,
            width - halfStroke,
            height - halfStroke,
            radii,
            Path.Direction.CW
        )
    }

    override fun rebuildClipPathNotStroke(path: Path) {
        if (width < 1 || height < 1) {
            return
        }
        path.addRoundRect(
            0F,
            0F,
            width.toFloat(),
            height.toFloat(),
            radius,
            Path.Direction.CW
        )
    }

}