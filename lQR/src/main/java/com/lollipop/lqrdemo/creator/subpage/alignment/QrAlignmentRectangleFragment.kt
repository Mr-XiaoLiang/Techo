package com.lollipop.lqrdemo.creator.subpage.alignment

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
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.base.PigmentTheme
import com.lollipop.lqrdemo.creator.HistoryColor
import com.lollipop.lqrdemo.creator.PaletteDialog
import com.lollipop.lqrdemo.creator.layer.AlignmentWriterLayer
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustContentFragment
import com.lollipop.lqrdemo.databinding.FragmentQrAlignmentRectangleBinding
import com.lollipop.lqrdemo.view.CheckedButtonBackgroundDrawable
import com.lollipop.pigment.Pigment
import com.lollipop.widget.RoundBackgroundView
import java.util.LinkedList

class QrAlignmentRectangleFragment : StyleAdjustContentFragment() {

    companion object {

        private val BORDER_RADIUS: Radius = Radius()
        private val CORE_RADIUS: Radius = Radius()

        @ColorInt
        private var BORDER_COLOR: Int = Color.BLACK

        @ColorInt
        private var CORE_COLOR: Int = Color.BLACK

        const val RADIUS_MAX = 1F
        const val RADIUS_MIN = 0F

    }

    private val binding: FragmentQrAlignmentRectangleBinding by lazyBind()

    private val checkedMap = HashMap<Int, Boolean>()
    private val historyColorAdapter = HistoryColorAdapter(::onColorChanged, ::onPaletteClick) {
        currentPigment
    }

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

    override fun onResume() {
        super.onResume()
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
        changeColor(color)
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
        changeRadius(value)
        notifyContentChanged()
    }

    private fun changeColor(
        value: Int
    ) {
        if (binding.leftTopCoreCornerButton.isChecked ||
            binding.rightTopCoreCornerButton.isChecked ||
            binding.rightBottomCoreCornerButton.isChecked ||
            binding.leftBottomCoreCornerButton.isChecked
        ) {
            CORE_COLOR = value
        }

        if (
            binding.leftTopBorderCornerButton.isChecked ||
            binding.rightTopBorderCornerButton.isChecked ||
            binding.rightBottomBorderCornerButton.isChecked ||
            binding.leftBottomBorderCornerButton.isChecked
        ) {
            BORDER_COLOR = value
        }
    }

    private fun changeRadius(
        value: Float
    ) {
        changeRadius(
            info = CORE_RADIUS,
            value = value,
            leftTop = binding.leftTopCoreCornerButton.isChecked,
            rightTop = binding.rightTopCoreCornerButton.isChecked,
            rightBottom = binding.rightBottomCoreCornerButton.isChecked,
            leftBottom = binding.leftBottomCoreCornerButton.isChecked,
        )

        changeRadius(
            info = BORDER_RADIUS,
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

    class Layer : BitMatrixWriterLayer(), AlignmentWriterLayer {

        private val borderRadius = Radius()
        private val coreRadius = Radius()
        private var borderColor = Color.BLACK
        private var coreColor = Color.BLACK

        override val positionBoundsEnable = true

        private val positionPathList = ArrayList<PositionInfo>()
        private val recyclePathList = LinkedList<PositionInfo>()

        private val contentPaint = Paint()

        override fun drawAlignment(canvas: Canvas) {
            checkContentPath()
            positionPathList.forEach {
                drawPosition(canvas, it.borderPath, it.corePath)
            }
        }

        private fun drawPosition(canvas: Canvas, border: Path, core: Path) {
            if (!border.isEmpty) {
                contentPaint.setColor(borderColor)
                contentPaint.style = Paint.Style.STROKE
                contentPaint.strokeWidth = scaleValue
                canvas.drawPath(border, contentPaint)
            }
            if (!core.isEmpty) {
                contentPaint.setColor(coreColor)
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

        private fun buildContentPath() {
            coreRadius.copyFrom(CORE_RADIUS)
            borderRadius.copyFrom(BORDER_RADIUS)
            coreColor = CORE_COLOR
            borderColor = BORDER_COLOR
            while (positionPathList.size < alignmentPatternBounds.size) {
                if (recyclePathList.isNotEmpty()) {
                    positionPathList.add(recyclePathList.removeFirst())
                } else {
                    positionPathList.add(PositionInfo(Path(), Path()))
                }
            }
            while (positionPathList.isNotEmpty()
                && positionPathList.size > alignmentPatternBounds.size
            ) {
                recyclePathList.add(positionPathList.removeAt(0))
            }
            for (index in positionPathList.indices) {
                val rect = alignmentPatternBounds[index]
                val info = positionPathList[index]
                updatePositionPointPath(
                    rect = rect,
                    border = info.borderPath,
                    core = info.corePath,
                    coreRadius = coreRadius,
                    borderRadius = borderRadius
                )
            }
        }

        private fun checkContentPath() {
            coreColor = CORE_COLOR
            borderColor = BORDER_COLOR
            if (!coreRadius.isSame(CORE_RADIUS)
                || !borderRadius.isSame(BORDER_RADIUS)
                || alignmentPatternBounds.size != positionPathList.size
            ) {
                buildContentPath()
            }
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

        private class PositionInfo(
            val borderPath: Path,
            val corePath: Path
        )

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

    private class HistoryColorAdapter(
        private val onColorClick: (Int) -> Unit,
        private val onPaletteClick: () -> Unit,
        private val getPigment: () -> Pigment?,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TYPE_COLOR = 0
            private const val TYPE_PALETTE = 1
        }

        private val colorList = ArrayList<Int>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == TYPE_PALETTE) {
                return PaletteHolder.create(parent, onPaletteClick)
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
            if (holder is PaletteHolder) {
                holder.bind(getPigment())
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
                    36.dp2px,
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
        private val iconView: ImageView,
        private val onClickCallback: () -> Unit
    ) : RecyclerView.ViewHolder(view) {

        companion object {
            fun create(parent: ViewGroup, onClickCallback: () -> Unit): PaletteHolder {
                val frameLayout = FrameLayout(parent.context)
                frameLayout.layoutParams = ViewGroup.LayoutParams(
                    48.dp2px,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                val iconView = ImageView(parent.context)
                val size = 24.dp2px
                iconView.layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    gravity = Gravity.CENTER
                }
                iconView.setImageResource(R.drawable.ic_baseline_palette_24)
                frameLayout.addView(iconView)
                return PaletteHolder(frameLayout, iconView, onClickCallback)
            }
        }

        init {
            itemView.onClick {
                onClickCallback()
            }
        }

        fun bind(pigment: Pigment?) {
            if (pigment == null) {
                iconView.imageTintList = null
            } else {
                iconView.imageTintList
                ColorStateList.valueOf(pigment.onBackgroundTitle)
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