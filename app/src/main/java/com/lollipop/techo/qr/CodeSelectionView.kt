package com.lollipop.techo.qr

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View.OnClickListener
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.base.util.ListenerManager
import com.lollipop.qr.reader.BarcodeWrapper
import com.lollipop.techo.R
import kotlin.math.max

class CodeSelectionView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : AppCompatImageView(context, attributeSet, style) {

    private val boundsDrawable = BoundsDrawable()

    private val codeBounds = ArrayList<Rect>()
    private val codeResultList = ArrayList<BarcodeWrapper>()

    private var navigateIcon: Drawable? = null

    private var navigateIconTint: ColorStateList? = null

    private val touchDownLocation = Point()

    private val onViewClickListener = OnClickListener {
        onViewClick()
    }

    private val codeSelectedListenerManager = ListenerManager<OnCodeSelectedListener>()

    var navigateIconSize = 0
        set(value) {
            field = value
            updateNavigateIcon()
        }

    init {
        setOnClickListener(onViewClickListener)
        scaleType = ScaleType.CENTER_CROP
        foreground = boundsDrawable
        attributeSet?.let { attr ->
            val typeArray = context.obtainStyledAttributes(attr, R.styleable.CodeSelectionView)
            color = typeArray.getColor(R.styleable.CodeSelectionView_android_color, Color.BLACK)
            radius = typeArray.getDimensionPixelSize(
                R.styleable.CodeSelectionView_android_radius, 0
            ).toFloat()
            strokeWidth = typeArray.getDimensionPixelSize(
                R.styleable.CodeSelectionView_codeStrokeWidth, 0
            ).toFloat()
            spaceWeight = typeArray.getFloat(
                R.styleable.CodeSelectionView_codeSpaceWeight, 0.5F
            )
            navigateIcon = typeArray.getDrawable(R.styleable.CodeSelectionView_codeNavigationIcon)
            navigateIconSize = typeArray.getDimensionPixelSize(
                R.styleable.CodeSelectionView_codeNavigationIconSize, 0
            )
            val tintColor = typeArray.getColor(
                R.styleable.CodeSelectionView_codeNavigationIconTint, 0
            )
            navigateIconTint = if (tintColor != 0) {
                ColorStateList.valueOf(tintColor)
            } else {
                null
            }
            navigateIcon?.setTintList(navigateIconTint)
            typeArray.recycle()
        }
    }

    var color: Int
        get() {
            return boundsDrawable.color
        }
        set(value) {
            boundsDrawable.color = value
        }

    var strokeWidth: Float
        get() {
            return boundsDrawable.strokeWidth
        }
        set(value) {
            boundsDrawable.strokeWidth = value
        }

    var radius: Float
        get() {
            return boundsDrawable.radius
        }
        set(value) {
            boundsDrawable.radius = value
        }

    var spaceWeight: Float
        get() {
            return boundsDrawable.spaceWeight
        }
        set(value) {
            boundsDrawable.spaceWeight = value
        }

    fun onCodeResult(size: Size, list: List<BarcodeWrapper>) {
        codeResultList.clear()
        codeBounds.clear()
        val viewWidth = width
        val viewHeight = height
        val srcWidth = size.width
        val srcHeight = size.height
        val weight = max(viewWidth * 1F / srcWidth, viewHeight * 1F / srcHeight)
        val offsetX = (viewWidth - srcWidth * weight) / 2
        val offsetY = (viewHeight - srcHeight * weight) / 2

        val boundsList = list.map { it.describe.boundingBox.copyOf(weight, offsetX, offsetY) }

        codeResultList.addAll(list)
        codeBounds.addAll(boundsList)

        boundsDrawable.setRectList(boundsList)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
            touchDownLocation.set(event.x.toInt(), event.y.toInt())
        }
        return super.onTouchEvent(event)
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val frame = super.setFrame(l, t, r, b)
        updateNavigateIcon()
        return frame
    }

    fun setNavigateIcon(drawable: Drawable?) {
        navigateIcon?.callback = null
        drawable?.callback = this
        navigateIcon = drawable
        drawable?.setTintList(navigateIconTint)
        updateNavigateIcon()
        invalidate()
    }

    fun tintNavigateIcon(color: ColorStateList?) {
        navigateIconTint = color
        navigateIcon?.setTintList(color)
        invalidate()
    }

    fun addOnCodeSelectedListener(listener: OnCodeSelectedListener) {
        codeSelectedListenerManager.addListener(listener)
    }

    fun removeOnCodeSelectedListener(listener: OnCodeSelectedListener) {
        codeSelectedListenerManager.removeListener(listener)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            drawNavigateIcon(canvas)
        }
    }

    private fun drawNavigateIcon(canvas: Canvas) {
        if (navigateIconSize < 1) {
            return
        }
        val icon = navigateIcon ?: return
        val halfIconSize = navigateIconSize / 2
        codeBounds.forEach {
            val x = it.centerX() - halfIconSize
            val y = it.centerY() - halfIconSize
            val saveCount = canvas.save()
            canvas.translate(x.toFloat(), y.toFloat())
            icon.draw(canvas)
            canvas.restoreToCount(saveCount)
        }
    }

    private fun updateNavigateIcon() {
        if (width > 0 && height > 0) {
            navigateIcon?.setBounds(0, 0, navigateIconSize, navigateIconSize)
        }
    }

    private fun onViewClick() {
        val touchX = touchDownLocation.x
        val touchY = touchDownLocation.y
        for (i in codeBounds.indices) {
            val bounds = codeBounds[i]
            if (bounds.contains(touchX, touchY)) {
                val code = codeResultList[i]
                codeSelectedListenerManager.invoke { it.onCodeSelected(code) }
                break
            }
        }
    }

    private fun Rect.copyOf(weight: Float, offsetX: Float, offsetY: Float): Rect {
        val src = this
        return Rect(
            src.left.offset(weight, offsetX),
            src.top.offset(weight, offsetY),
            src.right.offset(weight, offsetX),
            src.bottom.offset(weight, offsetY),
        )
    }

    private fun Int.offset(weight: Float, offset: Float): Int {
        val value = this
        return (value * weight + offset).toInt()
    }

    fun interface OnCodeSelectedListener {
        fun onCodeSelected(code: BarcodeWrapper)
    }

    private class BoundsDrawable : Drawable() {

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
        }

        var color: Int
            get() {
                return paint.color
            }
            set(value) {
                paint.color = value
            }

        var strokeWidth: Float
            get() {
                return paint.strokeWidth
            }
            set(value) {
                paint.strokeWidth = value
            }

        var radius = 0F

        var spaceWeight = 0.5F

        private val boundsPath = Path()

        private val rectList = ArrayList<Rect>()

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            buildPath()
        }

        fun setRectList(list: List<Rect>) {
            rectList.clear()
            rectList.addAll(list)
            buildPath()
            invalidateSelf()
        }

        private fun buildPath() {
            boundsPath.reset()
            if (bounds.isEmpty) {
                return
            }
            rectList.forEach {
                addRect(it, boundsPath)
            }
        }

        /**
         * ⎡ ⎤
         * ⎣ ⎦
         */
        private fun addRect(rect: Rect, path: Path) {
            val width = rect.width() - strokeWidth
            val height = rect.height() - strokeWidth
            val halfStroke = strokeWidth * 0.5F
            val halfRadius = radius * 0.5F
            val left = rect.left + halfStroke
            val top = rect.top + halfStroke
            val right = rect.right - halfStroke
            val bottom = rect.bottom - halfStroke

            val vLength = height * (1 - spaceWeight) / 2 - radius
            val hLength = width * (1 - spaceWeight) / 2 - radius

            // 左上角
            path.moveTo(left, top + vLength + radius)
            path.lineTo(left, top + radius)
            path.cubicTo(
                left, top + halfRadius,
                left + halfRadius, top,
                left + radius, top
            )
            path.lineTo(left + radius + hLength, top)

            // 右上角
            path.moveTo(right - radius - hLength, top)
            path.lineTo(right - radius, top)
            path.cubicTo(right - halfRadius, top, right, top + halfRadius, right, top + radius)
            path.lineTo(right, top + radius + vLength)

            // 右下角
            path.moveTo(right, bottom - radius - vLength)
            path.lineTo(right, bottom - radius)
            path.cubicTo(
                right, bottom - halfRadius,
                right - halfRadius, bottom,
                right - radius, bottom
            )
            path.lineTo(right - radius - hLength, bottom)

            // 左下角
            path.moveTo(left + radius + hLength, bottom)
            path.lineTo(left + radius, bottom)
            path.cubicTo(
                left + halfRadius, bottom,
                left, bottom - halfRadius,
                left, bottom - radius
            )
            path.lineTo(left, bottom - radius - vLength)
        }

        override fun draw(canvas: Canvas) {
            if (boundsPath.isEmpty) {
                return
            }
            canvas.drawPath(boundsPath, paint)
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

}