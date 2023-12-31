package com.lollipop.lqrdemo.creator.subpage.position

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.lollipop.base.util.dp2px
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.lqrdemo.base.PigmentTheme
import com.lollipop.lqrdemo.creator.HistoryColor
import com.lollipop.lqrdemo.creator.PaletteDialog
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.layer.PositionWriterLayer
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustContentFragment
import com.lollipop.lqrdemo.databinding.FragmentQrPositionRectangleBinding
import com.lollipop.lqrdemo.view.CheckedButtonBackgroundDrawable
import com.lollipop.pigment.Pigment
import com.lollipop.widget.RoundBackgroundView

class QrPositionRectangleFragment : StyleAdjustContentFragment() {

    companion object {

        private val LEFT_TOP_INFO = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        private val LEFT_BOTTOM_INFO = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        private val RIGHT_TOP_INFO = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        const val RADIUS_MAX = 1F
        const val RADIUS_MIN = 0F

    }

    private val binding: FragmentQrPositionRectangleBinding by lazyBind()

    private val checkedMap = HashMap<Int, Boolean>()
    private val historyColorAdapter = HistoryColorAdapter(::onColorChanged, ::onPaletteClick)

    private val sliderBackPressure = Runnable {
        invokeSliderChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCheckableButton(binding.leftTopPositionButton)
        initCheckableButton(binding.rightTopPositionButton)
        initCheckableButton(binding.leftBottomPositionButton)

        initCheckableButton(binding.leftTopBorderCornerButton)
        initCheckableButton(binding.rightTopBorderCornerButton)
        initCheckableButton(binding.rightBottomBorderCornerButton)
        initCheckableButton(binding.leftBottomBorderCornerButton)

        initCheckableButton(binding.leftTopCoreCornerButton)
        initCheckableButton(binding.rightTopCoreCornerButton)
        initCheckableButton(binding.rightBottomCoreCornerButton)
        initCheckableButton(binding.leftBottomCoreCornerButton)

        binding.radiusSlider.value = 0F
        binding.radiusSlider.addOnChangeListener(
            Slider.OnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    postSliderBackPressure()
                }
            }
        )

        binding.colorGroup.layoutManager = LinearLayoutManager(
            view.context, RecyclerView.HORIZONTAL, false
        )
        binding.colorGroup.adapter = historyColorAdapter
        historyColorAdapter.onColorListChanged(HistoryColor.get())
    }

    override fun getWriterLayer(): Class<out BitMatrixWriterLayer> {
        return Layer::class.java
    }

    private fun initCheckableButton(view: View) {
        view.updateCheckedState()
        view.onClick {
            view.checkedToggle()
            if (view.isChecked) {
                onSelectedCornerChanged()
            }
        }
    }

    private fun onSelectedCornerChanged() {
        invokeSliderChanged()
    }

    private fun onColorChanged(color: Int) {
        HistoryColor.put(color)
        historyColorAdapter.onColorListChanged(HistoryColor.get())

        if (binding.leftTopPositionButton.isChecked) {
            changeColor(LEFT_TOP_INFO, color)
        }
        if (binding.rightTopPositionButton.isChecked) {
            changeColor(RIGHT_TOP_INFO, color)
        }
        if (binding.leftBottomPositionButton.isChecked) {
            changeColor(LEFT_BOTTOM_INFO, color)
        }
        notifyContentChanged()
    }

    private fun onPaletteClick() {
        context?.let { ctx ->
            PaletteDialog.show(ctx, historyColorAdapter.findFirstColor()) { color ->
                onColorChanged(color)
            }
        }
    }

    private fun postSliderBackPressure() {
        binding.radiusSlider.removeCallbacks(sliderBackPressure)
        binding.radiusSlider.postDelayed(sliderBackPressure, 100)
    }

    private fun invokeSliderChanged() {
        // slider里面是0~100的，我们需要的是0~1，所以需要缩小100倍
        val value = binding.radiusSlider.value * 0.01F
        if (binding.leftTopPositionButton.isChecked) {
            changeRadius(LEFT_TOP_INFO, value)
        }
        if (binding.rightTopPositionButton.isChecked) {
            changeRadius(RIGHT_TOP_INFO, value)
        }
        if (binding.leftBottomPositionButton.isChecked) {
            changeRadius(LEFT_BOTTOM_INFO, value)
        }
        notifyContentChanged()
    }

    private fun changeColor(
        info: PositionInfo,
        value: Int
    ) {
        if (binding.leftTopCoreCornerButton.isChecked ||
            binding.rightTopCoreCornerButton.isChecked ||
            binding.rightBottomCoreCornerButton.isChecked ||
            binding.leftBottomCoreCornerButton.isChecked
        ) {
            info.coreColor = value
        }

        if (
            binding.leftTopBorderCornerButton.isChecked ||
            binding.rightTopBorderCornerButton.isChecked ||
            binding.rightBottomBorderCornerButton.isChecked ||
            binding.leftBottomBorderCornerButton.isChecked
        ) {
            info.borderColor = value
        }
    }

    private fun changeRadius(
        info: PositionInfo,
        value: Float
    ) {
        changeRadius(
            info = info.coreRadius,
            value = value,
            leftTop = binding.leftTopCoreCornerButton.isChecked,
            rightTop = binding.rightTopCoreCornerButton.isChecked,
            rightBottom = binding.rightBottomCoreCornerButton.isChecked,
            leftBottom = binding.leftBottomCoreCornerButton.isChecked,
        )

        changeRadius(
            info = info.borderRadius,
            value = value,
            leftTop = binding.leftTopBorderCornerButton.isChecked,
            rightTop = binding.rightTopBorderCornerButton.isChecked,
            rightBottom = binding.rightBottomBorderCornerButton.isChecked,
            leftBottom = binding.leftBottomBorderCornerButton.isChecked,
        )
    }

    private fun changeRadius(
        info: Radius,
        value: Float,
        leftTop: Boolean,
        rightTop: Boolean,
        rightBottom: Boolean,
        leftBottom: Boolean,
    ) {
        if (leftTop) {
            info.leftTopX = value
            info.leftTopY = value
        }
        if (rightTop) {
            info.rightTopX = value
            info.rightTopY = value
        }
        if (rightBottom) {
            info.rightBottomX = value
            info.rightBottomY = value
        }
        if (leftBottom) {
            info.leftBottomX = value
            info.leftBottomY = value
        }
    }

    private fun View.checkedToggle() {
        // 读取状态并反转
        val view = this
        view.isChecked = !view.isChecked
    }

    private fun View.updateCheckedState() {
        // 走一遍方法，让状态更新
        val view = this
        view.isChecked = view.isChecked
    }

    private var View.isChecked: Boolean
        get() {
            val view = this
            if (view.id == View.NO_ID) {
                return false
            }
            return checkedMap[view.id] ?: false
        }
        set(value) {
            val view = this
            if (view.id == View.NO_ID) {
                view.alpha = 0.5F
                return
            }
            checkedMap[view.id] = value
            view.alpha = if (value) {
                1F
            } else {
                0.5F
            }
        }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        pigment.blendMode.startFlow(pigment.primaryColor)
            .blend(pigment.onBackgroundTitle) { color ->
                checkOrUpdateButton(
                    binding.leftTopPositionButton,
                    binding.leftTopPositionIcon,
                    color
                )
                checkOrUpdateButton(
                    binding.leftBottomPositionButton,
                    binding.leftBottomPositionIcon,
                    color
                )
                checkOrUpdateButton(
                    binding.rightTopPositionButton,
                    binding.rightTopPositionIcon,
                    color
                )

                checkOrUpdateButton(
                    binding.leftTopBorderCornerButton,
                    binding.leftTopBorderCornerIcon,
                    color
                )
                checkOrUpdateButton(
                    binding.rightTopBorderCornerButton,
                    binding.rightTopBorderCornerIcon,
                    color
                )
                checkOrUpdateButton(
                    binding.rightBottomBorderCornerButton,
                    binding.rightBottomBorderCornerIcon,
                    color
                )
                checkOrUpdateButton(
                    binding.leftBottomBorderCornerButton,
                    binding.leftBottomBorderCornerIcon,
                    color
                )

                checkOrUpdateButton(
                    binding.leftTopCoreCornerButton,
                    binding.leftTopCoreCornerIcon,
                    color
                )
                checkOrUpdateButton(
                    binding.rightTopCoreCornerButton,
                    binding.rightTopCoreCornerIcon,
                    color
                )
                checkOrUpdateButton(
                    binding.rightBottomCoreCornerButton,
                    binding.rightBottomCoreCornerIcon,
                    color
                )
                checkOrUpdateButton(
                    binding.leftBottomCoreCornerButton,
                    binding.leftBottomCoreCornerIcon,
                    color
                )
                PigmentTheme.updateSlider(binding.radiusSlider, color)
            }
    }

    private fun checkOrUpdateButton(btnView: View, iconView: ImageView, color: Int) {
        iconView.imageTintList = ColorStateList.valueOf(color)
        val background = btnView.background
        if (background is CheckedButtonBackgroundDrawable) {
            background.color = color
        } else {
            val drawable = CheckedButtonBackgroundDrawable()
            drawable.color = color
            btnView.background = drawable
        }
    }

    class Layer : BitMatrixWriterLayer(), PositionWriterLayer {

        private val leftTopInfo = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        private val leftBottomInfo = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        private val rightTopInfo = PositionInfo(
            borderRadius = Radius(),
            coreRadius = Radius(),
            borderColor = Color.BLACK,
            coreColor = Color.BLACK
        )

        override val positionBoundsEnable = true

        private val borderLeftTopPath = Path()
        private val coreLeftTopPath = Path()

        private val borderRightTopPath = Path()
        private val coreRightTopPath = Path()

        private val borderLeftBottomPath = Path()
        private val coreLeftBottomPath = Path()

        private val contentPaint = Paint()

        override fun drawPosition(canvas: Canvas) {
            checkContentPath()
            drawPosition(canvas, borderLeftTopPath, coreLeftTopPath, leftTopInfo)
            drawPosition(canvas, borderRightTopPath, coreRightTopPath, rightTopInfo)
            drawPosition(canvas, borderLeftBottomPath, coreLeftBottomPath, leftBottomInfo)
        }

        private fun drawPosition(canvas: Canvas, border: Path, core: Path, info: PositionInfo) {
            if (!border.isEmpty) {
                contentPaint.setColor(info.borderColor)
                contentPaint.style = Paint.Style.STROKE
                contentPaint.strokeWidth = scaleValue
                canvas.drawPath(border, contentPaint)
            }
            if (!core.isEmpty) {
                contentPaint.setColor(info.coreColor)
                contentPaint.style = Paint.Style.FILL
                canvas.drawPath(core, contentPaint)
            }
        }

        override fun onBitMatrixChanged() {
            super.onBitMatrixChanged()
            buildContentPath()
        }

        override fun onBoundsChanged(bounds: Rect) {
            super.onBoundsChanged(bounds)
            buildContentPath()
        }

        private fun buildContentPath(
            leftTop: Boolean = true,
            rightTop: Boolean = true,
            leftBottom: Boolean = true
        ) {
            if (leftTop) {
                leftTopInfo.copyFrom(LEFT_TOP_INFO)
                updatePositionPointPath(
                    rect = positionBounds.leftTopPattern,
                    border = borderLeftTopPath,
                    core = coreLeftTopPath,
                    coreRadius = leftTopInfo.coreRadius,
                    borderRadius = leftTopInfo.borderRadius
                )
            }
            if (rightTop) {
                rightTopInfo.copyFrom(RIGHT_TOP_INFO)
                updatePositionPointPath(
                    rect = positionBounds.rightTopPattern,
                    border = borderRightTopPath,
                    core = coreRightTopPath,
                    coreRadius = rightTopInfo.coreRadius,
                    borderRadius = rightTopInfo.borderRadius
                )
            }
            if (leftBottom) {
                leftBottomInfo.copyFrom(LEFT_BOTTOM_INFO)
                updatePositionPointPath(
                    rect = positionBounds.leftBottomPattern,
                    border = borderLeftBottomPath,
                    core = coreLeftBottomPath,
                    coreRadius = leftBottomInfo.coreRadius,
                    borderRadius = leftBottomInfo.borderRadius
                )
            }
        }

        private fun checkContentPath() {
            buildContentPath(
                leftTop = (borderLeftTopPath.isEmpty ||
                        coreLeftTopPath.isEmpty ||
                        !leftTopInfo.isSame(LEFT_TOP_INFO)),
                rightTop = (borderRightTopPath.isEmpty ||
                        coreRightTopPath.isEmpty ||
                        !rightTopInfo.isSame(RIGHT_TOP_INFO)),
                leftBottom = (borderLeftBottomPath.isEmpty ||
                        coreLeftBottomPath.isEmpty ||
                        !leftBottomInfo.isSame(LEFT_BOTTOM_INFO)),
            )
        }

        private fun updatePositionPointPath(
            rect: Rect,
            border: Path,
            core: Path,
            coreRadius: Radius,
            borderRadius: Radius
        ) {
            border.reset()
            core.reset()
            val lineWidth = scaleValue
            addBorderToPath(
                path = border,
                rect = rect,
                lineWidth = lineWidth,
                radius = borderRadius
            )
            addCoreToPath(path = core, rect = rect, coreOffset = lineWidth * 2, radius = coreRadius)
        }

        private fun addBorderToPath(path: Path, rect: Rect, lineWidth: Float, radius: Radius) {
            val leftEdgeByScale = getLeftEdgeByScale(rect.left.toFloat())
            val topEdgeByScale = getTopEdgeByScale(rect.top.toFloat())
            val rightEdgeByScale = getRightEdgeByScale(rect.right.toFloat())
            val bottomEdgeByScale = getBottomEdgeByScale(rect.bottom.toFloat())
            val half = lineWidth * 0.5F
            // 添加外框
            val left = leftEdgeByScale + half
            val top = topEdgeByScale + half
            val right = rightEdgeByScale - half
            val bottom = bottomEdgeByScale - half
            path.addRoundRect(
                left,
                top,
                right,
                bottom,
                radius.pixelSize(right - left, bottom - top),
                Path.Direction.CW
            )
        }

        private fun addCoreToPath(path: Path, rect: Rect, coreOffset: Float, radius: Radius) {
            val leftEdgeByScale = getLeftEdgeByScale(rect.left.toFloat())
            val topEdgeByScale = getTopEdgeByScale(rect.top.toFloat())
            val rightEdgeByScale = getRightEdgeByScale(rect.right.toFloat())
            val bottomEdgeByScale = getBottomEdgeByScale(rect.bottom.toFloat())
            val left = leftEdgeByScale + coreOffset
            val top = topEdgeByScale + coreOffset
            val right = rightEdgeByScale - coreOffset
            val bottom = bottomEdgeByScale - coreOffset
            // 中心
            path.addRoundRect(
                left,
                top,
                right,
                bottom,
                radius.pixelSize(right - left, bottom - top),
                Path.Direction.CW
            )
        }

    }

    private class PositionInfo(
        val borderRadius: Radius,
        val coreRadius: Radius,
        @ColorInt
        var borderColor: Int,
        @ColorInt
        var coreColor: Int
    ) {

        fun copyFrom(info: PositionInfo) {
            this.borderRadius.copyFrom(info.borderRadius)
            this.coreRadius.copyFrom(info.coreRadius)
            this.borderColor = info.borderColor
            this.coreColor = info.coreColor
        }

        fun isSame(info: PositionInfo): Boolean {
            return (this.borderRadius.isSame(info.borderRadius) &&
                    this.coreRadius.isSame(info.coreRadius) &&
                    this.borderColor == info.borderColor &&
                    this.coreColor == info.coreColor
                    )
        }

    }

    private class HistoryColorAdapter(
        private val onColorClick: (Int) -> Unit,
        private val onPaletteClick: () -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TYPE_COLOR = 0
            private const val TYPE_PALETTE = 1
        }

        private val colorList = ArrayList<Int>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == TYPE_PALETTE) {
                return PaletteHolder.crate(parent, onPaletteClick)
            }
            return ColorHolder.crate(parent, ::onItemClick)
        }

        private fun onItemClick(position: Int) {
            if (position < 0 || position >= colorList.size) {
                return
            }
            val color = colorList[position]
            onColorClick(color)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun onColorListChanged(list: List<Int>) {
            colorList.clear()
            colorList.addAll(list)
            notifyDataSetChanged()
        }

        fun findFirstColor(): Int {
            if (colorList.isNotEmpty()) {
                return colorList[0]
            }
            return Color.RED
        }

        override fun getItemCount(): Int {
            return colorList.size + 1
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ColorHolder) {
                holder.bind(colorList[position])
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position >= 0 && position < colorList.size) {
                return TYPE_COLOR
            }
            return TYPE_PALETTE
        }

    }

    private class ColorHolder(
        view: View,
        private val colorView: RoundBackgroundView,
        private val onClickCallback: (Int) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        companion object {
            fun crate(parent: ViewGroup, onClickCallback: (Int) -> Unit): ColorHolder {
                val frameLayout = FrameLayout(parent.context)
                frameLayout.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                val colorView = RoundBackgroundView(parent.context)
                colorView.type = RoundBackgroundView.RoundType.SMALLER
                colorView.value = 0.5F
                val margin = 6.dp2px
                colorView.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    marginStart = margin
                    marginEnd = margin
                }
                frameLayout.addView(colorView)
                return ColorHolder(frameLayout, colorView, onClickCallback)
            }
        }

        init {
            colorView.onClick {
                onItemViewClick()
            }
        }

        private fun onItemViewClick() {
            onClickCallback(adapterPosition)
        }

        fun bind(color: Int) {
            colorView.color = color
        }

    }

    private class PaletteHolder(
        view: View,
        private val onClickCallback: () -> Unit
    ) : RecyclerView.ViewHolder(view) {

        companion object {
            fun crate(parent: ViewGroup, onClickCallback: () -> Unit): PaletteHolder {
                val frameLayout = FrameLayout(parent.context)
                frameLayout.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                val colorView = ImageView(parent.context)
                val size = 24.dp2px
                colorView.layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    gravity = Gravity.CENTER
                }
                frameLayout.addView(colorView)
                return PaletteHolder(frameLayout, onClickCallback)
            }
        }

        init {
            itemView.onClick {
                onClickCallback()
            }
        }

    }

    private class Radius(
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftTopX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftTopY: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightTopX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightTopY: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightBottomX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var rightBottomY: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftBottomX: Float = 0F,
        @FloatRange(from = RADIUS_MIN.toDouble(), to = RADIUS_MAX.toDouble())
        var leftBottomY: Float = 0F,
    ) {

        fun array(): FloatArray {
            return floatArrayOf(
                leftTopX,
                leftTopY,
                rightTopX,
                rightTopY,
                rightBottomX,
                rightBottomY,
                leftBottomX,
                leftBottomY,
            )
        }

        fun pixelSize(width: Float, height: Float): FloatArray {
            return floatArrayOf(
                leftTopX * width,
                leftTopY * height,
                rightTopX * width,
                rightTopY * height,
                rightBottomX * width,
                rightBottomY * height,
                leftBottomX * width,
                leftBottomY * height,
            )
        }

        fun copyFrom(info: Radius) {
            this.leftTopX = info.leftTopX
            this.leftTopY = info.leftTopY
            this.rightTopX = info.rightTopX
            this.rightTopY = info.rightTopY
            this.rightBottomX = info.rightBottomX
            this.rightBottomY = info.rightBottomY
            this.leftBottomX = info.leftBottomX
            this.leftBottomY = info.leftBottomY
        }

        fun isSame(info: Radius): Boolean {
            return (this.leftTopX == info.leftTopX &&
                    this.leftTopY == info.leftTopY &&
                    this.rightTopX == info.rightTopX &&
                    this.rightTopY == info.rightTopY &&
                    this.rightBottomX == info.rightBottomX &&
                    this.rightBottomY == info.rightBottomY &&
                    this.leftBottomX == info.leftBottomX &&
                    this.leftBottomY == info.leftBottomY
                    )
        }

    }

}