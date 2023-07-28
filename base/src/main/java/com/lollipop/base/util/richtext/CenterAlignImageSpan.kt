package com.lollipop.base.util.richtext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

/**
 * @author lollipop
 * @date 2021/10/11 21:11
 */
class CenterAlignImageSpan : ImageSpan {

    private var mDrawableRef: WeakReference<Drawable>? = null

    constructor(mDrawable: Drawable) : super(mDrawable) {
        mDrawableRef = WeakReference(mDrawable)
    }

    constructor(context: Context, drawableId: Int) : super(context, drawableId) {
        ContextCompat.getDrawable(context, drawableId)?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            mDrawableRef = WeakReference(it)
        }
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val d = getCachedDrawable()
        val rect = d.bounds

        if (fm != null) {
            fm.ascent = -rect.bottom
            fm.descent = 0

            fm.top = fm.ascent
            fm.bottom = 0
        }

        return rect.right
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val b = getCachedDrawable()
        canvas.save()

        val fm = paint.fontMetricsInt

        /**
         * y is the baseline
         *
         * text bounds : [0, y - ABS(ascent), textWidth, y + ABS(descent)] , ABS(ascent) = -fm.ascent, ABS(descent) = fm.descent
         *  -> [0, y + fm.ascent, textWidth, y + fm.descent]
         *  center of text : (y + fm.ascent + y + fm.descent) / 2
         *  so the transY = y + (fm.descent + fm.ascent) / 2 - b.bounds.bottom / 2
         */
        val transY = y + (fm.descent + fm.ascent) / 2 - b.bounds.bottom / 2

        canvas.translate(x, transY.toFloat())
        b.draw(canvas)
        canvas.restore()
    }

    override fun getDrawable(): Drawable {
        return mDrawableRef?.get() ?: super.getDrawable()
    }

    private fun getCachedDrawable(): Drawable {
        val wr = mDrawableRef
        var d: Drawable? = null

        if (wr != null)
            d = wr.get()

        if (d == null) {
            d = drawable
            mDrawableRef = WeakReference(d)
        }
        return d
    }
}