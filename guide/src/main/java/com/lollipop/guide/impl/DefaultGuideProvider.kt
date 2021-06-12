package com.lollipop.guide.impl

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.FrameLayout
import com.lollipop.guide.GuideProvider
import com.lollipop.guide.GuideStep

/**
 * @author lollipop
 * @date 2021/6/1 23:10
 * 默认的引导提供者
 */
class DefaultGuideProvider : GuideProvider() {

    companion object {
        val DEFAULT_GUIDE_STYLE = GuideStyle(
            backgroundColor = Color.BLACK and 0xFFFFFF or 0xAA000000.toInt(),
            cornerDp = 10,
            marginVerticalDp = 10,
            marginHorizontalDp = 10,
            paddingVerticalDp = 10,
            paddingHorizontalDp = 10,
            spaceToTargetDp = 10
        )

        var guideStyle = DEFAULT_GUIDE_STYLE
    }

    override fun support(step: GuideStep): Boolean {
        return true
    }

    override fun onGuideBoundsChanged(left: Int, top: Int, right: Int, bottom: Int) {
        TODO("Not yet implemented")
    }

    override fun onTargetBoundsChange(left: Int, top: Int, right: Int, bottom: Int) {
        TODO("Not yet implemented")
    }

    override fun onTargetChange(step: GuideStep) {
        TODO("Not yet implemented")
    }

    private class GuideView(context: Context) : FrameLayout(context) {

        private val clipOutDrawable = ClipOutDrawable()

        var corner: Float
            set(value) {
                clipOutDrawable.corner = value
            }
            get() {
                return clipOutDrawable.corner
            }

        var isOval: Boolean
            set(value) {
                clipOutDrawable.isOval = value
            }
            get() {
                return clipOutDrawable.isOval
            }

        var color: Int
            set(value) {
                clipOutDrawable.color = value
            }
            get() {
                return clipOutDrawable.color
            }

        fun changeTargetBounds(left: Int, top: Int, right: Int, bottom: Int) {
            clipOutDrawable.changeTargetBounds(left, top, right, bottom)
        }

        init {
            background = clipOutDrawable
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            TODO("Not yet implemented")
        }
    }

    private class ClipOutDrawable : Drawable() {

        var corner = 0F
            set(value) {
                field = value
                needUpdatePath = true
            }

        var isOval = false
            set(value) {
                field = value
                needUpdatePath = true
            }

        private val targetBounds = RectF()

        private val clipPath = Path()

        private var needUpdatePath = true

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
        }

        var color: Int
            get() {
                return paint.color
            }
            set(value) {
                paint.color = value
            }

        fun changeTargetBounds(left: Int, top: Int, right: Int, bottom: Int) {
            targetBounds.set(
                left.toFloat(),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat()
            )
            updateClipPath()
        }

        private fun updateClipPath() {
            clipPath.reset()
            if (isOval) {
                clipPath.addOval(
                    targetBounds.left,
                    targetBounds.top,
                    targetBounds.right,
                    targetBounds.bottom,
                    Path.Direction.CW
                )
            } else {
                clipPath.addRoundRect(
                    targetBounds.left,
                    targetBounds.top,
                    targetBounds.right,
                    targetBounds.bottom,
                    corner,
                    corner,
                    Path.Direction.CW
                )
            }
            needUpdatePath = false
        }

        override fun draw(canvas: Canvas) {
            val saveCount = canvas.saveCount
            if (needUpdatePath) {
                updateClipPath()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                canvas.clipOutPath(clipPath)
            } else {
                canvas.clipPath(clipPath, Region.Op.DIFFERENCE)
            }
            canvas.drawRect(bounds, paint)
            canvas.restoreToCount(saveCount)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

    }

    data class GuideStyle(
        val backgroundColor: Int,
        val cornerDp: Int,
        val marginVerticalDp: Int,
        val marginHorizontalDp: Int,
        val paddingVerticalDp: Int,
        val paddingHorizontalDp: Int,
        val spaceToTargetDp: Int
    )

}