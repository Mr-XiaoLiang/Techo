package com.lollipop.palette

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

class ColorWheelView(
    context: Context,
    attributeSet: AttributeSet?,
    style: Int
) : AppCompatImageView(context, attributeSet, style) {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    companion object {
        private val hueColor by lazy {
            IntArray(361).apply {
                for (i in indices) {
                    this[i] = Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
                }
            }
        }
    }

    private var slideBarWidth = 1F
    private var slideBarInterval = 1F

    private var valueSlideBarColor = Color.BLACK
    private var alphaSlideBarColor = Color.BLACK

    var alphaSlideBarEnable = false
        set(value) {
            field = value
            layoutWheel()
        }
    private var wheelRadius = 0F
    private var wheelCenter = PointF()

    private var slideBarRadius: Float = 0F

    private var hsv = HSV()

    private var hueShader: Shader? = null
    private var wheelShader: Shader? = null

    private val wheelPaint by lazy {
        Paint().apply {
            isDither = true
            isAntiAlias = true
        }
    }

    init {
        attributeSet?.let { attrs ->
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ColorWheelView)
            slideBarWidth = typeArray.getDimensionPixelSize(
                R.styleable.ColorWheelView_slideBarWidth, 0
            ).toFloat()
            slideBarInterval = typeArray.getDimensionPixelSize(
                R.styleable.ColorWheelView_slideBarInterval, 0
            ).toFloat()
            valueSlideBarColor = typeArray.getColor(
                R.styleable.ColorWheelView_valueSlideBarColor, Color.BLACK
            )
            alphaSlideBarColor = typeArray.getColor(
                R.styleable.ColorWheelView_alphaSlideBarColor, Color.BLACK
            )
            typeArray.recycle()
        }
        if (isInEditMode) {
            hsv.v = 0.5F
            hsv.a = 0.4F
            alphaSlideBarEnable = true
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        layoutWheel()
    }

    private fun layoutWheel() {
        val w = width
        val h = height
        if (w < 1 || h < 1) {
            return
        }
        val maxW = w * 1F - paddingLeft - paddingRight
        val maxH = h * 1F - paddingTop - paddingBottom
        var wheelMaxWidth = maxW - slideBarWidth - slideBarInterval
        if (alphaSlideBarEnable) {
            wheelMaxWidth -= slideBarWidth + slideBarInterval
        }
        val wheelDiameter = min(maxH, wheelMaxWidth)
        wheelRadius = wheelDiameter * 0.5F
        slideBarRadius = wheelRadius + slideBarInterval + (slideBarWidth * 0.5F)
        wheelCenter.y = (maxH * 0.5F) + paddingTop
        var contentWidth = wheelDiameter + slideBarWidth + slideBarInterval
        if (alphaSlideBarEnable) {
            contentWidth += slideBarWidth + slideBarInterval
        }
        wheelCenter.x = ((maxW - contentWidth) * 0.5F) + paddingLeft + wheelRadius
        if (alphaSlideBarEnable) {
            wheelCenter.x += slideBarWidth + slideBarInterval
        }

        hueShader = SweepGradient(wheelCenter.x, wheelCenter.y, hueColor, null)
        buildSaturation()
    }

    private fun buildSaturation() {
        val hue = hueShader
        if (hue == null) {
            wheelShader = null
            return
        }
        val rgb = max(0, min(255, (hsv.v * 255).toInt()))
        val color = Color.rgb(rgb, rgb, rgb)
        val saturationShader = RadialGradient(
            wheelCenter.x,
            wheelCenter.y,
            wheelRadius,
            color.alpha(0F),
            color,
            Shader.TileMode.CLAMP
        )
        wheelShader = ComposeShader(hue, saturationShader, PorterDuff.Mode.MULTIPLY)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        // 绘制圆盘
        drawHuePanel(canvas)
        // 绘制明度条
        drawValueBar(canvas)
        // 绘制透明度条
        drawAlphaBar(canvas)
    }

    private fun drawHuePanel(canvas: Canvas) {
        wheelPaint.style = Paint.Style.FILL
        wheelPaint.shader = wheelShader
        canvas.drawOval(
            wheelCenter.x - wheelRadius,
            wheelCenter.y - wheelRadius,
            wheelCenter.x + wheelRadius,
            wheelCenter.y + wheelRadius,
            wheelPaint
        )
    }

    private fun drawValueBar(canvas: Canvas) {
        wheelPaint.style = Paint.Style.STROKE
        wheelPaint.strokeCap = Paint.Cap.ROUND
        wheelPaint.strokeWidth = slideBarWidth
        wheelPaint.shader = null
        // 绘制背景
        wheelPaint.color = valueSlideBarColor.alpha(0.3F)
        canvas.drawArc(
            wheelCenter.x - slideBarRadius,
            wheelCenter.y - slideBarRadius,
            wheelCenter.x + slideBarRadius,
            wheelCenter.y + slideBarRadius,
            -45F,
            90F,
            false,
            wheelPaint
        )
        // 绘制前景
        wheelPaint.color = valueSlideBarColor
        val sweep = 90 * hsv.v
        val start = 45 - sweep
        canvas.drawArc(
            wheelCenter.x - slideBarRadius,
            wheelCenter.y - slideBarRadius,
            wheelCenter.x + slideBarRadius,
            wheelCenter.y + slideBarRadius,
            start,
            sweep,
            false,
            wheelPaint
        )
    }

    private fun drawAlphaBar(canvas: Canvas) {
        wheelPaint.style = Paint.Style.STROKE
        wheelPaint.strokeCap = Paint.Cap.ROUND
        wheelPaint.strokeWidth = slideBarWidth
        wheelPaint.shader = null
        // 绘制背景
        wheelPaint.color = alphaSlideBarColor.alpha(0.3F)
        canvas.drawArc(
            wheelCenter.x - slideBarRadius,
            wheelCenter.y - slideBarRadius,
            wheelCenter.x + slideBarRadius,
            wheelCenter.y + slideBarRadius,
            135F,
            90F,
            false,
            wheelPaint
        )
        // 绘制前景
        wheelPaint.color = alphaSlideBarColor
        canvas.drawArc(
            wheelCenter.x - slideBarRadius,
            wheelCenter.y - slideBarRadius,
            wheelCenter.x + slideBarRadius,
            wheelCenter.y + slideBarRadius,
            135F,
            90 * hsv.a,
            false,
            wheelPaint
        )
    }

    private fun Int.alpha(float: Float): Int {
        val src = this
        val a = min(255, max(0, ((src ushr 24) * float).toInt()))
        return src and 0xFFFFFF or ((a % 256) shl 24)
    }

    private class HSV(
        var h: Float = 0F,
        var s: Float = 0F,
        var v: Float = 0F,
        var a: Float = 0F
    )

}