package com.lollipop.lqrdemo.creator.subpage.datapoint

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.slider.Slider
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.lqrdemo.base.PigmentTheme
import com.lollipop.lqrdemo.creator.HistoryColor
import com.lollipop.lqrdemo.creator.PaletteDialog
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.layer.ContentWriterLayer
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustContentFragment
import com.lollipop.lqrdemo.databinding.FragmentQrDataPointRectangleBinding
import com.lollipop.pigment.Pigment
import com.lollipop.qr.writer.LBitMatrix
import com.lollipop.qr.writer.LQrBitMatrix
import kotlin.math.min

class QrDataPointRectangleFragment : StyleAdjustContentFragment() {

    companion object {

        private val RADIUS: Radius = Radius()

        @ColorInt
        private var COLOR: Int = Color.BLACK

    }

    private val binding: FragmentQrDataPointRectangleBinding by lazyBind()

    private val historyColorAdapter =
        createHistoryColorAdapter(::onColorChanged, ::onPaletteClick) {
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
        changeRadius(
            info = RADIUS,
            value = value,
            leftTop = binding.leftTopCoreCornerButton.isChecked,
            rightTop = binding.rightTopCoreCornerButton.isChecked,
            rightBottom = binding.rightBottomCoreCornerButton.isChecked,
            leftBottom = binding.leftBottomCoreCornerButton.isChecked
        )
        notifyContentChanged()
    }

    private fun changeColor(value: Int) {
        COLOR = value
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

    class Layer : BitMatrixWriterLayer(), ContentWriterLayer {

        private val radius = Radius()
        private var color = Color.BLACK

        private val contentPath = Path()

        private val contentPaint = Paint()

        override fun drawContent(canvas: Canvas) {
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
            radius.copyFrom(RADIUS)
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
                            tempRect.set(x, y, x, y)
                            addPoint(path, tempRect)
                            y++
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

        private fun addPoint(path: Path, rect: Rect) {
            val leftEdgeByScale = getLeftEdgeByScale(rect.left.toFloat())
            val topEdgeByScale = getTopEdgeByScale(rect.top.toFloat())
            val rightEdgeByScale = getRightEdgeByScale(rect.right.toFloat())
            val bottomEdgeByScale = getBottomEdgeByScale(rect.bottom.toFloat())
            val d = min(
                rightEdgeByScale - leftEdgeByScale,
                bottomEdgeByScale - topEdgeByScale
            )
            path.addRoundRect(
                leftEdgeByScale,
                topEdgeByScale,
                rightEdgeByScale,
                bottomEdgeByScale,
                radius.pixelSize(d, d),
                Path.Direction.CW
            )
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

}