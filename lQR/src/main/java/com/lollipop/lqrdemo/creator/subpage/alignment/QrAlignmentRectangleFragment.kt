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

    }

    private val binding: FragmentQrAlignmentRectangleBinding by lazyBind()

    private val historyColorAdapter = createHistoryColorAdapter(::onColorChanged, ::onPaletteClick) {
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

    class Layer : BitMatrixWriterLayer(), AlignmentWriterLayer {

        private val borderRadius = Radius()
        private val coreRadius = Radius()
        private var borderColor = Color.BLACK
        private var coreColor = Color.BLACK

        override val alignmentPatternBoundsEnable = true

        private val positionPathList = ArrayList<PositionInfo>()
        private val recyclePathList = LinkedList<PositionInfo>()

        private val contentPaint = Paint()

        override fun drawAlignment(canvas: Canvas) {
            checkContentPath()
            positionPathList.forEach {
                drawAlignment(canvas, it.borderPath, it.corePath)
            }
        }

        private fun drawAlignment(canvas: Canvas, border: Path, core: Path) {
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

}