package com.lollipop.clip

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.widget.FrameLayout

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
    }

    fun setStrokeWidth(width: Int) {
        strokeWidth = width
        strokePaint.strokeWidth = width.toFloat()
        rebuildStrokePath()
        invalidate()
    }

    fun setStrokeColor(color: Int) {
        strokePaint.color = color
        invalidate()
    }

    fun setStrokeShader(shader: Shader?) {
        strokePaint.shader = shader
        invalidate()
    }

    protected fun rebuildStrokePath() {
        clipPathWithStroke.reset()
        rebuildClipPathWithStroke(clipPathWithStroke)
        invalidate()
    }

    protected fun rebuildDefaultPath() {
        clipPathNotStroke.reset()
        rebuildClipPathNotStroke(clipPathNotStroke)
        invalidate()
    }

    protected abstract fun rebuildClipPathWithStroke(path: Path)
    protected abstract fun rebuildClipPathNotStroke(path: Path)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        rebuildStrokePath()
        rebuildDefaultPath()
    }

    override fun draw(canvas: Canvas) {
        val drawStroke = isStrokeEnable && strokeWidth > 0
        val clipPath = if (drawStroke) {
            clipPathWithStroke
        } else {
            clipPathNotStroke
        }
        if (clipPath.isEmpty) {
            super.draw(canvas)
            return
        }
        val saveCount = canvas.save()
        canvas.clipPath(clipPath)
        super.draw(canvas)
        canvas.restoreToCount(saveCount)
        if (drawStroke) {
            canvas.drawPath(clipPath, strokePaint)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        val drawStroke = isStrokeEnable && strokeWidth > 0
        val clipPath = if (drawStroke) {
            clipPathWithStroke
        } else {
            clipPathNotStroke
        }
        if (clipPath.isEmpty) {
            super.dispatchDraw(canvas)
            return
        }
        val saveCount = canvas.save()
        canvas.clipPath(clipPath)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)
        if (drawStroke) {
            canvas.drawPath(clipPath, strokePaint)
        }
    }

}