package liang.lollipop.ltabview.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * @date: 2019-04-19 22:38
 * @author: lollipop
 * 可以做展开动画的Drawable
 */
class ExpandDrawable: Drawable() {

    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    var color: Int = Color.BLACK
        set(value) {
            field = value
            changeColorAlpha()
            invalidateSelf()
        }

    var progress = 1F
        set(value) {
            field = value
            changeColorAlpha()
            invalidateSelf()
        }

    var minWidth = 0

    private val boundsFloat = RectF()

    override fun draw(canvas: Canvas) {
        val width = (bounds.width() - minWidth) * progress + minWidth
        boundsFloat.set(bounds)
        val r = boundsFloat.height() / 2
        canvas.drawRoundRect(boundsFloat.left, boundsFloat.top,
            boundsFloat.left + width, boundsFloat.bottom,
            r, r, paint)
    }

    private fun changeColorAlpha() {
        val alpha = (Color.alpha(color) * progress).let {
            return@let when {
                it < 0 -> 0F
                it > 255 -> 255F
                else -> it
            }
        }.toInt()

        paint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun getOpacity() = PixelFormat.TRANSPARENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }
}