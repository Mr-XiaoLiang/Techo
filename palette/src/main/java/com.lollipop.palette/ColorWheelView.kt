package com.lollipop.palette

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.annotation.ColorInt
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

        private const val VALUE_SLIDE_START_ANGLE = -45
        private const val VALUE_SLIDE_END_ANGLE = 45

        private const val ALPHA_SLIDE_START_ANGLE = 135
        private const val ALPHA_SLIDE_END_ANGLE = 225

        private val hueColor by lazy {
            IntArray(361).apply {
                for (i in indices) {
                    this[i] = Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
                }
            }
        }
    }

    private var slideBarWidth = 0F
    private var slideBarInterval = 0F
    private var anchorRadius = 0F

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
    private var slideBarLength: Float = 0F

    private var hsv = HSV()

    private var hueShader: Shader? = null
    private var wheelShader: Shader? = null

    private var touchTarget = TouchTarget.NONE

    private val wheelPaint by lazy {
        Paint().apply {
            isDither = true
            isAntiAlias = true
        }
    }

    private val touchHelper = SingleTouchHelper(::onTouchDown, ::onTouchMove, ::onTouchUp)

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
            anchorRadius = typeArray.getDimensionPixelSize(
                R.styleable.ColorWheelView_anchorRadius, 0
            ).toFloat()
            alphaSlideBarEnable = typeArray.getBoolean(
                R.styleable.ColorWheelView_alphaSlideBarEnable, false
            )
            val color = typeArray.getColor(R.styleable.ColorWheelView_color, Color.RED)
            hsv.set(color)
            typeArray.recycle()
        }
    }

    fun reset(@ColorInt color: Int) {
        hsv.set(color)
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        layoutWheel()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        touchHelper.onTouchEvent(event)
        return touchTarget != TouchTarget.NONE
    }

    private fun onTouchDown(x: Float, y: Float) {
        val touchRadius = AngleCalculator.getLength(wheelCenter.x, wheelCenter.y, x, y)
        if (touchRadius <= wheelRadius) {
            touchTarget = TouchTarget.WHEEL
            return
        }
        val angle = getAngle(x, y)
        if (angle >= (VALUE_SLIDE_START_ANGLE + 360) || angle <= VALUE_SLIDE_END_ANGLE) {
            touchTarget = TouchTarget.VALUE
            return
        }
        if (alphaSlideBarEnable && angle >= ALPHA_SLIDE_START_ANGLE && angle <= ALPHA_SLIDE_END_ANGLE) {
            touchTarget = TouchTarget.ALPHA
            return
        }
        touchTarget = TouchTarget.NONE
    }

    private fun onTouchMove(x: Float, y: Float, offsetX: Float, offsetY: Float) {
        when (touchTarget) {
            TouchTarget.NONE -> {
                return
            }
            TouchTarget.ALPHA -> {
                onAlphaBarMove(x, y, offsetX, offsetY)
            }
            TouchTarget.VALUE -> {
                onValueBarMove(x, y, offsetX, offsetY)
            }
            TouchTarget.WHEEL -> {
                onWheelMove(x, y, offsetX, offsetY)
            }
        }
        invalidate()
    }

    private fun onAlphaBarMove(x: Float, y: Float, offsetX: Float, offsetY: Float) {
        val radius = getRadius(x, y)
        val weight = max((radius - slideBarRadius) / slideBarWidth, 1F)
        val offsetAlpha = offsetY * -1 / slideBarLength / weight
        hsv.offsetA(offsetAlpha)
    }

    private fun onValueBarMove(x: Float, y: Float, offsetX: Float, offsetY: Float) {
        val radius = getRadius(x, y)
        val weight = max((radius - slideBarRadius) / slideBarWidth, 1F)
        val offsetValue = offsetY * -1 / slideBarLength / weight
        hsv.offsetV(offsetValue)
    }

    private fun onWheelMove(x: Float, y: Float, offsetX: Float, offsetY: Float) {
        // 手指所在半径
        val radius = getRadius(x, y)
        // 通过半径计算手指距离圆盘的距离，然后得到移动距离的缩放权重
        val weight = max((radius - wheelRadius) / slideBarWidth, 1F)
        // 通过当前参数计算当前的坐标
        val current = getLocation(wheelRadius * hsv.s, hsv.h)
        // 通过坐标叠加位置偏移量
        current[0] += offsetX
        current[1] += offsetY
        // 移动后的焦点半径
        val newRadius = getRadius(current[0], current[1])
        // 移动后的焦点角度
        val newAngle = getAngle(current[0], current[1])
        Log.d("Lollipop", "onWheelMove: [$x, $y], $radius, $newRadius")
        // 设置HS的参数
        hsv.resetS(newRadius / wheelRadius)
        hsv.resetH(newAngle)
    }

    private fun onTouchUp(cancel: Boolean) {
        touchTarget = TouchTarget.NONE
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
        slideBarLength = AngleCalculator.getArcLength(slideBarRadius, 90F)
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

    private fun getRadius(x: Float, y: Float): Float {
        return AngleCalculator.getLength(wheelCenter.x, wheelCenter.y, x, y)
    }

    private fun getAngle(x: Float, y: Float): Float {
        return AngleCalculator.getCircumferential(wheelCenter.x, wheelCenter.y, x, y)
    }

    private fun getLocation(radius: Float, angle: Float): FloatArray {
        return AngleCalculator.getCoordinate(wheelCenter.x, wheelCenter.y, radius, angle)
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
        // 绘制选中锚点
        drawAnchor(canvas)
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
        val radius = slideBarRadius
        canvas.drawArc(
            wheelCenter.x - radius,
            wheelCenter.y - radius,
            wheelCenter.x + radius,
            wheelCenter.y + radius,
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
            wheelCenter.x - radius,
            wheelCenter.y - radius,
            wheelCenter.x + radius,
            wheelCenter.y + radius,
            start,
            sweep,
            false,
            wheelPaint
        )
    }

    private fun drawAlphaBar(canvas: Canvas) {
        if (!alphaSlideBarEnable) {
            return
        }
        wheelPaint.style = Paint.Style.STROKE
        wheelPaint.strokeCap = Paint.Cap.ROUND
        wheelPaint.strokeWidth = slideBarWidth
        wheelPaint.shader = null
        // 绘制背景
        wheelPaint.color = alphaSlideBarColor.alpha(0.3F)
        val radius = slideBarRadius
        canvas.drawArc(
            wheelCenter.x - radius,
            wheelCenter.y - radius,
            wheelCenter.x + radius,
            wheelCenter.y + radius,
            135F,
            90F,
            false,
            wheelPaint
        )
        // 绘制前景
        wheelPaint.color = alphaSlideBarColor
        canvas.drawArc(
            wheelCenter.x - radius,
            wheelCenter.y - radius,
            wheelCenter.x + radius,
            wheelCenter.y + radius,
            135F,
            90 * hsv.a,
            false,
            wheelPaint
        )
    }

    private fun drawAnchor(canvas: Canvas) {
        wheelPaint.shader = null
        wheelPaint.color = hsv.color
        wheelPaint.style = Paint.Style.FILL
        val angle = hsv.h
        val radius = hsv.s * wheelRadius
        val saveCount = canvas.save()
        canvas.translate(wheelCenter.x, wheelCenter.y)
        canvas.rotate(angle)
        val xOffset = anchorRadius
        canvas.drawOval(
            radius - xOffset,
            -xOffset,
            radius + xOffset,
            xOffset,
            wheelPaint
        )
        canvas.restoreToCount(saveCount)
        wheelPaint.alpha = 255
    }

    private fun Int.alpha(float: Float): Int {
        val src = this
        val a = min(255, max(0, ((src ushr 24) * float).toInt()))
        return src and 0xFFFFFF or ((a % 256) shl 24)
    }

    private class HSV {

        var h: Float = 0F
            private set
        var s: Float = 0F
            private set
        var v: Float = 0F
            private set
        var a: Float = 0F
            private set

        fun set(color: Int) {
            val hsvArray = FloatArray(3)
            Color.colorToHSV(color, hsvArray)
            h = hsvArray[0]
            s = hsvArray[1]
            v = hsvArray[2]
            a = Color.alpha(color) / 255F
        }

        fun set(hValue: Float, sValue: Float, vValue: Float, aValue: Float) {
            resetS(hValue)
            resetV(sValue)
            resetA(vValue)
            resetH(aValue)
        }

        val color: Int
            get() {
                return Color.HSVToColor((a * 255).toInt(), floatArrayOf(h, s, v))
            }

        fun resetH(value: Float) {
            h = rangeTo(value, 360F)
        }

        fun resetS(value: Float) {
            s = rangeTo(value, 1F)
        }

        fun resetV(value: Float) {
            v = rangeTo(value, 1F)
        }

        fun resetA(value: Float) {
            a = rangeTo(value, 1F)
        }

        fun offsetV(offset: Float) {
            resetV(v + offset)
        }

        fun offsetA(offset: Float) {
            resetA(a + offset)
        }

        private fun rangeTo(src: Float, max: Float): Float {
            if (src < 0F) {
                return 0F
            }
            if (src > max) {
                return max
            }
            return src
        }

    }

    private enum class TouchTarget {
        NONE,
        ALPHA,
        VALUE,
        WHEEL;
    }

}