package com.lollipop.faceicon

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView

class FaceIconView @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null
) : AppCompatImageView(context, attributeSet) {

    private val faceIconDrawable = FaceIconDrawable()

    var color: Int
        get() {
            return faceIconDrawable.color
        }
        set(value) {
            faceIconDrawable.color = value
        }

    var shader: Shader?
        get() {
            return faceIconDrawable.shader
        }
        set(value) {
            faceIconDrawable.shader = value
        }

    var strokeWidth: Float
        get() {
            return faceIconDrawable.strokeWidth
        }
        set(value) {
            faceIconDrawable.strokeWidth = value
        }

    var animationDuration: Long
        set(value) {
            animator.animationDuration = value
        }
        get() {
            return animator.animationDuration
        }

    private val animator = FaceAnimator(::postNextFace, ::progress)

    init {
        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.FaceIconView)
        color = typeArray.getColor(R.styleable.FaceIconView_faceColor, Color.WHITE)
        strokeWidth = typeArray.getDimensionPixelSize(R.styleable.FaceIconView_strokeWidth, 1).toFloat()
        val faceCode = typeArray.getInt(R.styleable.FaceIconView_faceIcon, 1)
        typeArray.recycle()
        super.setImageDrawable(faceIconDrawable)
        nextFace(
            when (faceCode) {
                1 -> FaceIcons.HAPPY
                2 -> FaceIcons.SADNESS
                3 -> FaceIcons.CALM
                else -> FaceIcons.HAPPY
            }
        )
    }

    private fun postNextFace(faceIcon: FaceIcon, progress: Float) {
        faceIconDrawable.next(faceIcon, progress)
    }

    fun nextFace(faceIcon: FaceIcon) {
        animator.post(faceIcon)
    }

    fun progress(progress: Float) {
        faceIconDrawable.progress(progress)
    }

    fun update() {
        faceIconDrawable.rebuild()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.end()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(faceIconDrawable)
    }

}