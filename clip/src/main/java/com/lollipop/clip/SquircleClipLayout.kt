package com.lollipop.clip

import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import com.lollipop.clip.squircle.RectangleSquircle
import com.lollipop.clip.squircle.SquircleCorner
import androidx.core.content.withStyledAttributes

/**
 * 超椭圆的剪裁工具类
 */
class SquircleClipLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ClipLayout(context, attrs) {

    private val rectangleSquircle = RectangleSquircle()

    private val rectangleSquircleStroke = RectangleSquircle()

    init {
        context.withStyledAttributes(attrs, R.styleable.SquircleClipLayout) {
            val defRadius = getDimensionPixelSize(
                R.styleable.SquircleClipLayout_android_radius, 0
            )
            val defWeight = getFloat(
                R.styleable.SquircleClipLayout_radiusWeight, 0.8F
            )

            val leftTop = getDimensionPixelSize(
                R.styleable.SquircleClipLayout_leftTop, defRadius
            ).toFloat()
            val rightTop = getDimensionPixelSize(
                R.styleable.SquircleClipLayout_rightTop, defRadius
            ).toFloat()
            val rightBottom = getDimensionPixelSize(
                R.styleable.SquircleClipLayout_rightBottom, defRadius
            ).toFloat()
            val leftBottom = getDimensionPixelSize(
                R.styleable.SquircleClipLayout_leftBottom, defRadius
            ).toFloat()

            val leftTopWeight = getFloat(
                R.styleable.SquircleClipLayout_leftTopWeight, defWeight
            )
            val rightTopWeight = getFloat(
                R.styleable.SquircleClipLayout_rightTopWeight, defWeight
            )
            val rightBottomWeight = getFloat(
                R.styleable.SquircleClipLayout_rightBottomWeight, defWeight
            )
            val leftBottomWeight = getFloat(
                R.styleable.SquircleClipLayout_leftBottomWeight, defWeight
            )

            setCorner(
                leftTop = SquircleCorner(
                    inEdgeRadius = leftTop,
                    outEdgeRadius = leftTop,
                    inEdgeWeight = leftTopWeight,
                    outEdgeWeight = leftTopWeight
                ),
                rightTop = SquircleCorner(
                    inEdgeRadius = rightTop,
                    outEdgeRadius = rightTop,
                    inEdgeWeight = rightTopWeight,
                    outEdgeWeight = rightTopWeight
                ),
                rightBottom = SquircleCorner(
                    inEdgeRadius = rightBottom,
                    outEdgeRadius = rightBottom,
                    inEdgeWeight = rightBottomWeight,
                    outEdgeWeight = rightBottomWeight
                ),
                leftBottom = SquircleCorner(
                    inEdgeRadius = leftBottom,
                    outEdgeRadius = leftBottom,
                    inEdgeWeight = leftBottomWeight,
                    outEdgeWeight = leftBottomWeight
                )
            )
        }
    }

    fun setCorner(
        leftTop: SquircleCorner,
        rightTop: SquircleCorner,
        rightBottom: SquircleCorner,
        leftBottom: SquircleCorner
    ) {
        rectangleSquircle.setCorner(leftTop, rightTop, rightBottom, leftBottom)
    }

    private fun buildStrokeCorner() {
        if (width < 1 || height < 1) {
            return
        }
        val strokeOffset = strokeWidth * -0.5F
        rectangleSquircleStroke.setCorner(
            leftTop = rectangleSquircle.leftTop.offset(
                inRadiusOffset = strokeOffset,
                outRadiusOffset = strokeOffset
            ),
            rightTop = rectangleSquircle.rightTop.offset(
                inRadiusOffset = strokeOffset,
                outRadiusOffset = strokeOffset
            ),
            rightBottom = rectangleSquircle.rightBottom.offset(
                inRadiusOffset = strokeOffset,
                outRadiusOffset = strokeOffset
            ),
            leftBottom = rectangleSquircle.leftBottom.offset(
                inRadiusOffset = strokeOffset,
                outRadiusOffset = strokeOffset
            )
        )
    }

    override fun rebuildClipPathWithStroke(path: Path) {
        if (width < 1 || height < 1) {
            return
        }
        val halfStroke = strokeWidth / 2
        buildStrokeCorner()
        rectangleSquircleStroke.build(
            halfStroke,
            halfStroke,
            width - halfStroke,
            height - halfStroke,
            path
        )
    }

    override fun rebuildClipPathNotStroke(path: Path) {
        rectangleSquircle.build(
            0,
            0,
            width,
            height,
            path
        )
    }

}