package com.lollipop.lqrdemo.creator.subpage.datapoint

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
import com.lollipop.lqrdemo.databinding.FragmentQrDataPointRectangleBinding
import com.lollipop.lqrdemo.view.CheckedButtonBackgroundDrawable
import com.lollipop.pigment.Pigment
import com.lollipop.qr.writer.LBitMatrix
import com.lollipop.qr.writer.LQrBitMatrix
import com.lollipop.widget.RoundBackgroundView

class QrDataPointRectangleFragment : StyleAdjustContentFragment() {

    companion object {

        private val RADIUS: Radius = Radius()

        @ColorInt
        private var COLOR: Int = Color.BLACK

        const val RADIUS_MAX = 1F
        const val RADIUS_MIN = 0F

    }

    private val binding: FragmentQrDataPointRectangleBinding by lazyBind()

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
        initCheckableButton(binding.leftTopCoreCornerButton)
        initCheckableButton(binding.rightTopCoreCornerButton)
        initCheckableButton(binding.rightBottomCoreCornerButton)
        initCheckableButton(binding.leftBottomCoreCornerButton)

        binding.radiusSlider.value = 0F
        binding.radiusSlider.addOnChangeListener(
            Slider.OnChangeListener { _, _, fromUser ->
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

    private fun changeColor(value: Int) {
        COLOR = value
    }

    private fun changeRadius(
        value: Float,
    ) {
        val info = RADIUS
        val leftTop = binding.leftTopCoreCornerButton.isChecked
        val rightTop = binding.rightTopCoreCornerButton.isChecked
        val rightBottom = binding.rightBottomCoreCornerButton.isChecked
        val leftBottom = binding.leftBottomCoreCornerButton.isChecked
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

        private val radius = Radius()
        private var color = Color.BLACK

        private val contentPath = Path()

        private val contentPaint = Paint()

        override fun drawAlignment(canvas: Canvas) {
            checkContentPath()
            contentPaint.setColor(color)
            contentPaint.style = Paint.Style.FILL
            canvas.drawPath(contentPath, contentPaint)
        }

        override fun onBitMatrixChanged() {
            super.onBitMatrixChanged()
            updateDataPointPath()
        }

        override fun onBoundsChanged(bounds: Rect) {
            super.onBoundsChanged(bounds)
            updateDataPointPath()
        }

        private fun updateDataPointPath() {
            val path = contentPath
            path.reset()
            findQrBitMatrix { matrix ->
                val quietZone = matrix.quietZone
                val right = matrix.width - quietZone
                val bottom = matrix.height - quietZone
                val tempRect = Rect()
                for (x in quietZone until right) {
                    var y = quietZone
                    while (y < bottom) {
                        if (isInAlignmentPattern(matrix, x, y)) {
                            y++
                            continue
                        }
                        val currentType = matrix.getType(x, y)
                        if (currentType == LBitMatrix.Type.BLACK) {
                            val edge =
                                matrix.getVerticalEdge(x, y, LBitMatrix.Type.BLACK) { px, py ->
                                    !isInAlignmentPattern(matrix, px, py)
                                }
                            if (edge < 0) {
                                y++
                                continue
                            }
                            if (edge >= y) {
                                tempRect.set(x, y, x, edge)
                                addRectToPathByScale(path, tempRect)
                            }
                            y = edge + 1
                        } else {
                            val edge =
                                matrix.getVerticalEdge(x, y, LBitMatrix.Type.WHITE) { px, py ->
                                    !isInAlignmentPattern(matrix, px, py)
                                }
                            y = if (edge < 0) {
                                y + 1
                            } else {
                                edge + 1
                            }
                        }
                    }
                }
            }
        }

        private fun isInAlignmentPattern(matrix: LQrBitMatrix, x: Int, y: Int): Boolean {
            val quietZone = matrix.quietZone
            val realX = x - quietZone
            val realY = y - quietZone
            val width = matrix.width - quietZone - quietZone
            val height = matrix.height - quietZone - quietZone
            return LQrBitMatrix.inLeftTop(realX, realY)
                    || LQrBitMatrix.inRightTop(width, realX, realY)
                    || LQrBitMatrix.inLeftBottom(height, realX, realY)
                    || LQrBitMatrix.isAlignmentPattern(matrix.version, width, realX, realY)
        }

        private fun checkContentPath() {
            color = COLOR
            if (!radius.isSame(RADIUS)) {
                updateDataPointPath()
            }
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