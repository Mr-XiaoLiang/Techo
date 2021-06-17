package com.lollipop.guide.impl

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lollipop.guide.GuideProvider
import com.lollipop.guide.GuideStep
import kotlin.math.max
import kotlin.math.min

/**
 * @author lollipop
 * @date 2021/6/1 23:10
 * 默认的引导提供者
 */
class DefaultGuideProvider : GuideProvider() {

    companion object {
        val DEFAULT_GUIDE_STYLE = GuideStyle(
            backgroundColor = Color.BLACK and 0xFFFFFF or 0xAA000000.toInt(),
            popColor = Color.BLUE,
            popTextColor = Color.WHITE,
            cornerDp = 10,
            marginVerticalDp = 10,
            marginHorizontalDp = 10,
            paddingVerticalDp = 10,
            paddingHorizontalDp = 10,
            spaceToTargetDp = 10,
            textSizeSp = 16,
            clipIsOval = false
        )

        fun create(style: GuideStyle): DefaultGuideProvider {
            return DefaultGuideProvider().apply {
                guideStyle = style
            }
        }

    }

    private var guideStyle = DEFAULT_GUIDE_STYLE

    private var guideView: GuideView? = null

    override fun support(step: GuideStep): Boolean {
        return true
    }

    override fun onCreateView(group: ViewGroup): View {
        val view = guideView
        if (view == null) {
            val newView = GuideView(group.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            guideView = newView
            return newView
        }
        return view
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        guideView?.let { guide ->
            if (view == guide) {
                val corner = guideStyle.cornerDp.dp2px()
                guide.guideClipCorner = corner
                guide.guidePopCorner = corner
                guide.guideBackgroundColor = guideStyle.backgroundColor
                guide.guideClipIsOval = guideStyle.clipIsOval
                guide.guidePopColor = guideStyle.popColor
                guide.guidePopTextColor = guideStyle.popTextColor
                guide.guidePopTextSize = guideStyle.textSizeSp.sp2px()
                guide.popSpace = guideStyle.spaceToTargetDp.dp2px().toInt()
                val paddingVertical = guideStyle.paddingVerticalDp.dp2px().toInt()
                val paddingHorizontal = guideStyle.paddingHorizontalDp.dp2px().toInt()
                guide.setPopPadding(
                    paddingHorizontal,
                    paddingVertical,
                    paddingHorizontal,
                    paddingVertical
                )
                val marginVertical = guideStyle.marginVerticalDp.dp2px().toInt()
                val marginHorizontal = guideStyle.marginHorizontalDp.dp2px().toInt()
                guide.setPopMargin(
                    marginHorizontal,
                    marginVertical,
                    marginHorizontal,
                    marginVertical
                )
                guide.setOnClickListener {
                    callNextStep()
                }
            }
        }
    }

    override fun onGuideBoundsChanged(
        width: Int,
        height: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        val marginVertical = guideStyle.marginVerticalDp.dp2px().toInt()
        val marginHorizontal = guideStyle.marginHorizontalDp.dp2px().toInt()
        guideView?.setPopMargin(
            left + marginHorizontal,
            top + marginVertical,
            right + marginHorizontal,
            bottom + marginVertical
        )
        guideView?.invalidate()
    }

    override fun onTargetBoundsChange(left: Int, top: Int, right: Int, bottom: Int) {
        guideView?.changeTargetBounds(left, top, right, bottom)
        guideView?.invalidate()
    }

    override fun onTargetChange(step: GuideStep) {
        when (step) {
            is OvalGuideStep -> {
                guideView?.guideClipIsOval = true
            }
            is RoundRectGuideStep -> {
                guideView?.guideClipIsOval = false
            }
            else -> {
                guideView?.guideClipIsOval = guideStyle.clipIsOval
            }
        }
        guideView?.onStepChanged(step.info)
    }

    private class GuideView(context: Context) : ViewGroup(context) {

        private val clipOutDrawable = ClipOutDrawable()
        private val popBackground = PopDrawable()

        private val popTextView = TextView(context).apply {
            gravity = Gravity.CENTER
            background = popBackground
        }

        var guideClipCorner: Float
            set(value) {
                clipOutDrawable.corner = value
            }
            get() {
                return clipOutDrawable.corner
            }

        var guidePopCorner: Float
            set(value) {
                popBackground.corner = value
            }
            get() {
                return popBackground.corner
            }

        var guideClipIsOval: Boolean
            set(value) {
                clipOutDrawable.isOval = value
            }
            get() {
                return clipOutDrawable.isOval
            }

        var guideBackgroundColor: Int
            set(value) {
                clipOutDrawable.color = value
            }
            get() {
                return clipOutDrawable.color
            }

        var guidePopColor: Int
            set(value) {
                popBackground.color = value
            }
            get() {
                return popBackground.color
            }

        var guidePopTextColor: Int
            set(value) {
                popTextView.setTextColor(value)
            }
            get() {
                return popTextView.textColors.defaultColor
            }

        var guidePopTextSize: Float
            set(value) {
                popTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
            }
            get() {
                return popTextView.textSize
            }

        var popSpace = 0

        private val clipBounds: RectF
            get() {
                return clipOutDrawable.targetBounds
            }

        init {
            background = clipOutDrawable
            addView(popTextView)
        }

        fun onStepChanged(info: CharSequence) {
            popTextView.text = info
            requestLayout()
        }

        fun setPopPadding(left: Int, top: Int, right: Int, bottom: Int) {
            popTextView.setPadding(left, top, right, bottom)
        }

        fun setPopMargin(left: Int, top: Int, right: Int, bottom: Int) {
            setPadding(left, top, right, bottom)
        }

        fun changeTargetBounds(left: Int, top: Int, right: Int, bottom: Int) {
            clipOutDrawable.changeTargetBounds(left, top, right, bottom)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val maxChildHeight = max(
                clipBounds.top - paddingTop,
                heightSize - paddingBottom - clipBounds.bottom
            ).toInt() - popSpace
            val maxChildWidth = widthSize - paddingLeft - paddingRight

            val childWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(maxChildWidth, MeasureSpec.AT_MOST)
            val childHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(maxChildHeight, MeasureSpec.AT_MOST)

            for (index in 0 until childCount) {
                getChildAt(index).measure(childWidthMeasureSpec, childHeightMeasureSpec)
            }
            setMeasuredDimension(widthSize, heightSize)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

            val target = clipBounds
            val topSpace = target.top - paddingTop

            val maxChildHeight = max(
                topSpace,
                height - paddingBottom - target.bottom
            ).toInt() - popSpace
            val maxChildWidth = width - paddingLeft - paddingRight

            val minLeft = paddingLeft
            // 虽然理论上只会有一个child，但是为了软件健壮性，遍历所有child进行排列
            for (index in 0 until childCount) {
                val child = getChildAt(index)
                // 宽度不能超过最大宽度
                val childWidth = min(child.measuredWidth, maxChildWidth)
                // 高度不能超过最大高度
                val childHeight = min(child.measuredHeight, maxChildHeight)
                // child的顶部，如果上方空间够，那么优先放在上部
                val childTop = if (childHeight <= topSpace) {
                    target.top - popSpace - childHeight
                } else {
                    target.bottom + popSpace
                }.toInt()
                // 最大的左侧间隙（避免右侧超出屏幕
                val maxLeft = width - paddingRight - childWidth
                // child的左侧，需要在左侧边缘到右侧边缘的空间内
                val childLeft = min(
                    maxLeft,
                    max(minLeft, target.centerX().toInt() - (childWidth / 2))
                )
                // 对它进行排列
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
            }
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

        val targetBounds = RectF()

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

    private class PopDrawable : Drawable() {

        var corner = 0F
            set(value) {
                field = value
                needUpdatePath = true
            }

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

        private val drawBounds = RectF()

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            drawBounds.set(bounds)
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(drawBounds, corner, corner, paint)
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
        val popColor: Int,
        val popTextColor: Int,
        val cornerDp: Int,
        val marginVerticalDp: Int,
        val marginHorizontalDp: Int,
        val paddingVerticalDp: Int,
        val paddingHorizontalDp: Int,
        val spaceToTargetDp: Int,
        val textSizeSp: Int,
        val clipIsOval: Boolean
    )

    private fun Int.dp2px(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )
    }

    private fun Int.sp2px(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )
    }

}