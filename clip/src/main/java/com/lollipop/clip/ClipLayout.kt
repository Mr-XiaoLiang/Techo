package com.lollipop.clip

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.core.graphics.withClip

abstract class ClipLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {


    private val clipPathWithStroke = Path()
    private val clipPathNotStroke = Path()

    protected var strokeWidth = 0
        private set

    private val strokePaint by lazy {
        Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
        }
    }

    var isStrokeEnable = false
        set(value) {
            field = value
            invalidate()
        }

    var isOutlineMode = true
        set(value) {
            field = value
            onOutlineModeChanged()
            invalidateOutline()
        }

    private fun isOutlineEnabled(): Boolean {
        return isOutlineMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    init {
        attrs?.let { a ->
            val typeArray = context.obtainStyledAttributes(a, R.styleable.ClipLayout)
            setStrokeColor(
                typeArray.getColor(
                    R.styleable.ClipLayout_clipStrokeColor, Color.BLACK
                )
            )
            setStrokeWidth(
                typeArray.getDimensionPixelSize(
                    R.styleable.ClipLayout_clipStrokeWidth, 0
                )
            )
            isStrokeEnable = typeArray.getBoolean(
                R.styleable.ClipLayout_clipStrokeEnable, true
            )
            typeArray.recycle()
        }
        onOutlineModeChanged()
    }

    private fun onOutlineModeChanged() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && isOutlineMode) {
            clipToOutline = true
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(
                    view: View?,
                    outline: Outline?
                ) {
                    outline?.setPath(clipPathNotStroke)
                }
            }
        } else {
            clipToOutline = false
            outlineProvider = null
        }
    }

    fun setStrokeWidth(width: Int) {
        strokeWidth = width
        strokePaint.strokeWidth = width.toFloat()
        rebuildStrokePath()
        if (isOutlineEnabled()) {
            invalidateOutline()
        } else {
            invalidate()
        }
    }

    fun setStrokeColor(color: Int) {
        strokePaint.color = color
        if (isOutlineEnabled()) {
            invalidateOutline()
        } else {
            invalidate()
        }
    }

    fun setStrokeShader(shader: Shader?) {
        strokePaint.shader = shader
        if (isOutlineEnabled()) {
            invalidateOutline()
        } else {
            invalidate()
        }
    }

    protected fun rebuildStrokePath() {
        clipPathWithStroke.reset()
        rebuildClipPathWithStroke(clipPathWithStroke)
        if (isOutlineEnabled()) {
            invalidateOutline()
        } else {
            invalidate()
        }
    }

    protected fun rebuildDefaultPath() {
        clipPathNotStroke.reset()
        rebuildClipPathNotStroke(clipPathNotStroke)
        if (isOutlineEnabled()) {
            invalidateOutline()
        } else {
            invalidate()
        }
    }

    protected abstract fun rebuildClipPathWithStroke(path: Path)
    protected abstract fun rebuildClipPathNotStroke(path: Path)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        rebuildStrokePath()
        rebuildDefaultPath()
    }

    private fun isDrawStroke(): Boolean {
        return isStrokeEnable && strokeWidth > 0
    }

    private fun getOutlinePath(): Path {
        return if (isDrawStroke()) {
            clipPathWithStroke
        } else {
            clipPathNotStroke
        }
    }

    override fun draw(canvas: Canvas) {
        if (isOutlineEnabled()) {
            super.draw(canvas)
            return
        }
        val drawStroke = isDrawStroke()
        val clipPath = getOutlinePath()
        if (clipPath.isEmpty) {
            super.draw(canvas)
            return
        }
        canvas.withClip(clipPath) {
            super.draw(canvas)
        }
        if (drawStroke) {
            canvas.drawPath(clipPath, strokePaint)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        val drawStroke = isStrokeEnable && strokeWidth > 0
        if (isOutlineEnabled()) {
            super.dispatchDraw(canvas)
            if (drawStroke) {
                canvas.drawPath(clipPathWithStroke, strokePaint)
            }
            return
        }
        val clipPath = if (drawStroke) {
            clipPathWithStroke
        } else {
            clipPathNotStroke
        }
        if (clipPath.isEmpty) {
            super.dispatchDraw(canvas)
            return
        }
        canvas.withClip(clipPath) {
            super.dispatchDraw(canvas)
        }
        if (drawStroke) {
            canvas.drawPath(clipPath, strokePaint)
        }
    }

}