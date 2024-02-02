package com.lollipop.qr.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View.OnClickListener
import android.widget.ImageView.ScaleType.CENTER_CROP
import android.widget.ImageView.ScaleType.FIT_CENTER
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.base.util.ListenerManager
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import com.lollipop.qr.BarcodeFormat
import com.lollipop.qr.R
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.qr.comm.BarcodeWrapper
import com.lollipop.qr.comm.CodeDescribe
import kotlin.math.max
import kotlin.math.min

class CodeSelectionView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : AppCompatImageView(context, attributeSet, style) {

    private val boundsDrawable = BoundsDrawable()

    private val codeBounds = ArrayList<Rect>()
    private val cornerPoints = ArrayList<Point>()
    private val codeResultList = ArrayList<BarcodeWrapper>()

    private val touchDownLocation = Point()

    private val onViewClickListener = OnClickListener {
        onViewClick()
    }

    private val codeSelectedListenerManager = ListenerManager<OnCodeSelectedListener>()

    init {
        setOnClickListener(onViewClickListener)
        scaleType = CENTER_CROP
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
            maskColor = typeArray.getColor(
                R.styleable.CodeSelectionView_codeMaskColor, Color.TRANSPARENT
            )
            navigateIconSize = typeArray.getDimensionPixelSize(
                R.styleable.CodeSelectionView_codeNavigationIconSize, 0
            )
            setNavigateIcon(typeArray.getDrawable(R.styleable.CodeSelectionView_codeNavigationIcon))
            val tintColor = typeArray.getColor(
                R.styleable.CodeSelectionView_codeNavigationIconTint, 0
            )
            tintNavigateIcon(
                if (tintColor != 0) {
                    ColorStateList.valueOf(tintColor)
                } else {
                    null
                }
            )
            typeArray.recycle()
        }
        if (isInEditMode) {
            onCodeResult(
                Size(720, 1250),
                listOf(
                    BarcodeWrapper(
                        BarcodeInfo.Text().apply {
                            value = "Text"
                        },
                        CodeDescribe(
                            Rect(100, 100, 200, 200),
                            arrayOf(
                                Point(100, 100),
                                Point(100, 200),
                                Point(200, 100),
                                Point(200, 200)
                            ),
                            "Text",
                            BarcodeFormat.QR_CODE,
                            "Text".toByteArray()
                        )
                    )
                )
            )
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

    var navigateIconSize: Int
        set(value) {
            boundsDrawable.navigateIconSize = value
        }
        get() {
            return boundsDrawable.navigateIconSize
        }

    var maskColor: Int
        set(value) {
            boundsDrawable.maskColor = value
        }
        get() {
            return boundsDrawable.maskColor
        }

    override fun setScaleType(scaleType: ScaleType?) {
        if (scaleType != CENTER_CROP && scaleType != FIT_CENTER) {
            throw RuntimeException("只支持 CENTER_CROP 和 FIT_CENTER")
        }
        super.setScaleType(scaleType)
    }

    private fun getScale(size: Size): Scale {
        val viewWidth: Float = width.toFloat()
        val viewHeight: Float = height.toFloat()
        val srcWidth: Float = size.width.toFloat()
        val srcHeight: Float = size.height.toFloat()
        var weight: Float = 1F
        var offsetX: Float = 0F
        var offsetY: Float = 0F
        when (scaleType) {
            CENTER_CROP -> {
                // view = 100 * 100
                // src  = 200 * 300
                // weight = 0.5F
                // offsetX = 0F
                // offsetY = -12.5
                weight = max(viewWidth / srcWidth, viewHeight / srcHeight)
                offsetX = (viewWidth - srcWidth * weight) / 2
                offsetY = (viewHeight - srcHeight * weight) / 2
            }

            FIT_CENTER -> {
                // view = 100 * 100
                // src  = 200 * 300
                // weight = 0.3F
                // offsetX = 20F
                // offsetY = 0F
                weight = min(viewWidth / srcWidth, viewHeight / srcHeight)
                offsetX = (viewWidth - srcWidth * weight) / 2
                offsetY = (viewHeight - srcHeight * weight) / 2
            }

            else -> {
            }
        }
        val contentLeft = max(offsetX, 0F)
        val contentTop = max(offsetY, 0F)
        val contentWidth = min(viewWidth, srcWidth * weight)
        val contentHeight = min(viewHeight, srcHeight * weight)
        return Scale(weight, offsetX, offsetY, contentLeft, contentTop, contentWidth, contentHeight)
    }

    fun onCodeResult(size: Size, list: List<BarcodeWrapper>) {
        if (width < 1 || height < 1) {
            requestLayout()
            post {
                updateCodeInfo(size, list)
            }
            return
        }
        updateCodeInfo(size, list)
    }

    private fun updateCodeInfo(size: Size, list: List<BarcodeWrapper>) {
        val scale = getScale(size)
        val weight = scale.weight
        val offsetX = scale.offsetX
        val offsetY = scale.offsetY
        // 先清理
        codeResultList.clear()
        codeBounds.clear()
        cornerPoints.clear()
        // drawable的数据清理一下，避免出现上一次的位置闪现
        boundsDrawable.clearBoundsList()
        doAsync {
            val boundsList = ArrayList<Rect>(list.size)
            val pointList = ArrayList<Point>(list.size * 4)
            list.forEach { barcode ->
                val describe = barcode.describe
                boundsList.add(describe.boundingBox.copyOf(weight, offsetX, offsetY))
                describe.cornerPoints.forEach {
                    pointList.add(Point(it.x.offset(weight, offsetX), it.y.offset(weight, offsetY)))
                }
            }
            val contentBounds = scale.contentBounds()
            onUI {
                codeResultList.addAll(list)
                codeBounds.addAll(boundsList)
                cornerPoints.addAll(pointList)

                boundsDrawable.setRectList(contentBounds, boundsList)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.actionMasked == MotionEvent.ACTION_DOWN) {
            touchDownLocation.set(event.x.toInt(), event.y.toInt())
        }
        return super.onTouchEvent(event)
    }

    fun setNavigateIcon(drawable: Drawable?) {
        boundsDrawable.setNavigateIcon(drawable)
    }

    fun tintNavigateIcon(color: ColorStateList?) {
        boundsDrawable.tintNavigateIcon(color)
    }

    fun addOnCodeSelectedListener(listener: OnCodeSelectedListener) {
        codeSelectedListenerManager.addListener(listener)
    }

    fun removeOnCodeSelectedListener(listener: OnCodeSelectedListener) {
        codeSelectedListenerManager.removeListener(listener)
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

    private class Scale(
        val weight: Float,
        val offsetX: Float,
        val offsetY: Float,
        val contentLeft: Float,
        val contentTop: Float,
        val contentWidth: Float,
        val contentHeight: Float
    ) {

        fun contentBounds(): RectF {
            return RectF(
                contentLeft,
                contentTop,
                contentLeft + contentWidth,
                contentTop + contentHeight
            )
        }

    }

    private class BoundsDrawable : Drawable() {

        private var navigateIcon: Drawable? = null

        private val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
        }

        @ColorInt
        var color: Int = Color.BLACK

        @ColorInt
        var maskColor: Int = Color.TRANSPARENT

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

        private val photoBounds = RectF()

        var navigateIconSize = 0
            set(value) {
                field = value
                updateNavigateIcon()
            }

        private var navigateIconTint: ColorStateList? = null

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            buildPath()
            updateNavigateIcon()
        }

        fun setRectList(contentBounds: RectF, list: List<Rect>) {
            photoBounds.set(contentBounds)
            rectList.clear()
            rectList.addAll(list)
            buildPath()
            invalidateSelf()
        }

        fun clearBoundsList() {
            rectList.clear()
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

        fun setNavigateIcon(drawable: Drawable?) {
            navigateIcon?.callback = null
            drawable?.callback = this.callback
            navigateIcon = drawable
            drawable?.setTintList(navigateIconTint)
            updateNavigateIcon()
            invalidateSelf()
        }

        fun tintNavigateIcon(color: ColorStateList?) {
            navigateIconTint = color
            navigateIcon?.setTintList(color)
            invalidateSelf()
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
            if (rectList.isEmpty()) {
                return
            }
            if (boundsPath.isEmpty) {
                return
            }
            paint.color = maskColor
            paint.style = Paint.Style.FILL
            canvas.drawRect(photoBounds, paint)

            paint.color = color
            paint.style = Paint.Style.STROKE
            canvas.drawPath(boundsPath, paint)

            drawNavigateIcon(canvas)
        }

        private fun drawNavigateIcon(canvas: Canvas) {
            if (navigateIconSize < 1) {
                return
            }
            val icon = navigateIcon ?: return
            val halfIconSize = navigateIconSize / 2
            rectList.forEach {
                val x = it.centerX() - halfIconSize
                val y = it.centerY() - halfIconSize
                val saveCount = canvas.save()
                canvas.translate(x.toFloat(), y.toFloat())
                icon.draw(canvas)
                canvas.restoreToCount(saveCount)
            }
        }

        private fun updateNavigateIcon() {
            navigateIcon?.setBounds(0, 0, navigateIconSize, navigateIconSize)
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