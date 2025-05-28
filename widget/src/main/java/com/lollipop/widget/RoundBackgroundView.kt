package com.lollipop.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.lollipop.widget.RoundBackgroundView.RoundType.ABSOLUTE
import com.lollipop.widget.RoundBackgroundView.RoundType.HEIGHT_WEIGHT
import com.lollipop.widget.RoundBackgroundView.RoundType.OVAL
import com.lollipop.widget.RoundBackgroundView.RoundType.SMALLER
import com.lollipop.widget.RoundBackgroundView.RoundType.WIDTH_WEIGHT
import kotlin.math.min

class RoundBackgroundView @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null
) : FrameLayout(context, attributeSet) {

    private val roundDrawable = RoundDrawable()

    init {
        background?.let {
            if (it is ColorDrawable) {
                roundDrawable.color = it.color
            }
        }
        background = roundDrawable
        context.withStyledAttributes(
            attributeSet, R.styleable.RoundBackgroundView
        ) {
            val roundTypeValue = getInteger(
                R.styleable.RoundBackgroundView_round_type,
                SMALLER.ordinal
            )
            val radiusValue = getDimensionPixelSize(
                R.styleable.RoundBackgroundView_round_radius,
                0
            )
            val weightValue = getFloat(
                R.styleable.RoundBackgroundView_round_weight,
                0F
            )
            val colorValue = getColor(R.styleable.RoundBackgroundView_color, Color.WHITE)

            val roundType =
                if (roundTypeValue >= 0 && roundTypeValue < RoundType.entries.size) {
                    RoundType.entries[roundTypeValue]
                } else {
                    SMALLER
                }
            val roundValue: Float = when (roundType) {
                ABSOLUTE -> {
                    radiusValue.toFloat()
                }

                SMALLER,
                WIDTH_WEIGHT,
                HEIGHT_WEIGHT,
                OVAL -> {
                    weightValue
                }
            }
            roundDrawable.value = roundValue
            roundDrawable.type = roundType
            roundDrawable.color = colorValue
        }
    }

    var color: Int
        get() {
            return roundDrawable.color
        }
        set(value) {
            roundDrawable.color = value
            invalidate()
        }

    var shader: Shader
        get() {
            return roundDrawable.shader
        }
        set(value) {
            roundDrawable.shader = value
            invalidate()
        }

    var value: Float
        get() {
            return roundDrawable.value
        }
        set(value) {
            roundDrawable.value = value
            invalidate()
        }

    var type: RoundType
        get() {
            return roundDrawable.type
        }
        set(value) {
            roundDrawable.type = value
            invalidate()
        }

    private class RoundDrawable : Drawable() {

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
        }

        var color: Int
            get() {
                return paint.color
            }
            set(value) {
                paint.color = value
            }

        var shader: Shader
            get() {
                return paint.shader
            }
            set(value) {
                paint.shader = value
            }

        var value: Float = 0F

        var type: RoundType = SMALLER

        private val path = Path()
        private val boundsF = RectF()

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            boundsF.set(bounds)
            buildPath()
        }

        private fun buildPath() {
            if (boundsF.isEmpty) {
                return
            }
            path.reset()
            when (type) {
                SMALLER -> {
                    val radius = min(boundsF.width(), boundsF.height()) * value
                    path.addRoundRect(boundsF, radius, radius, Path.Direction.CW)
                }

                ABSOLUTE -> {
                    path.addRoundRect(boundsF, value, value, Path.Direction.CW)
                }

                WIDTH_WEIGHT -> {
                    val radius = boundsF.width() * value
                    path.addRoundRect(boundsF, radius, radius, Path.Direction.CW)
                }

                HEIGHT_WEIGHT -> {
                    val radius = boundsF.height() * value
                    path.addRoundRect(boundsF, radius, radius, Path.Direction.CW)
                }

                OVAL -> {
                    path.addOval(boundsF, Path.Direction.CW)
                }
            }

            invalidateSelf()
        }

        override fun draw(canvas: Canvas) {
            canvas.drawPath(path, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

    }

    enum class RoundType {

        SMALLER,
        ABSOLUTE,
        WIDTH_WEIGHT,
        HEIGHT_WEIGHT,
        OVAL

    }

}