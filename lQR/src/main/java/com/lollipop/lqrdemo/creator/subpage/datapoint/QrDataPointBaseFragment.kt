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
import com.lollipop.lqrdemo.databinding.FragmentQrDataPointBaseBinding
import com.lollipop.pigment.Pigment
import com.lollipop.qr.writer.LQrBitMatrix

abstract class QrDataPointBaseFragment : StyleAdjustContentFragment() {

    companion object {

        private val RADIUS: Radius = Radius()

        private var LAST_RADIUS_VALUE = RADIUS_MIN

        @ColorInt
        private var COLOR: Int = Color.BLACK

        private const val POINT_SIZE_MAX = 1F
        private const val POINT_SIZE_MIN = 0.3F

        private var POINT_SIZE = POINT_SIZE_MAX

        private val CHECKED_MAP = HashMap<Int, Boolean>()

    }

    private val binding: FragmentQrDataPointBaseBinding by lazyBind()

    private val historyColorAdapter =
        createHistoryColorAdapter(::onColorChanged, ::onPaletteClick) {
            currentPigment
        }

    private val radiusSliderBackPressure = Runnable {
        invokeRadiusSliderChanged()
    }

    private val sizeSliderBackPressure = Runnable {
        invokeSizeSliderChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    private fun Float.progressToValue(): Float {
        val value = this
        return value * 0.01F
    }

    private fun Float.valueToProgress(): Float {
        val value = this
        return value * 100
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCheckableButton(binding.leftTopCoreCornerButton)
        initCheckableButton(binding.rightTopCoreCornerButton)
        initCheckableButton(binding.rightBottomCoreCornerButton)
        initCheckableButton(binding.leftBottomCoreCornerButton)

        binding.radiusSlider.value = LAST_RADIUS_VALUE.valueToProgress()
        binding.radiusSlider.addOnChangeListener(
            Slider.OnChangeListener { _, _, fromUser ->
                if (fromUser) {
                    postRadiusSliderBackPressure()
                }
            }
        )

        binding.sizeSlider.value = POINT_SIZE.valueToProgress()
        binding.sizeSlider.addOnChangeListener(
            Slider.OnChangeListener { _, _, fromUser ->
                if (fromUser) {
                    postSizeSliderBackPressure()
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
        resetCheckedMap(CHECKED_MAP)
        updateCheckedStates(
            binding.leftTopCoreCornerButton,
            binding.rightTopCoreCornerButton,
            binding.rightBottomCoreCornerButton,
            binding.leftBottomCoreCornerButton,
        )
        binding.radiusSlider.value = LAST_RADIUS_VALUE.valueToProgress()
        binding.sizeSlider.value = POINT_SIZE.valueToProgress()
        historyColorAdapter.onColorListChanged(HistoryColor.get())
    }

    override fun onPause() {
        super.onPause()
        copyCheckedMap(CHECKED_MAP)
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
        invokeRadiusSliderChanged()
    }

    private fun onColorChanged(color: Int) {
        HistoryColor.put(color)
        historyColorAdapter.onColorListChanged(HistoryColor.get())
        COLOR = color
        notifyContentChanged()
    }

    private fun onPaletteClick() {
        context?.let { ctx ->
            PaletteDialog.show(ctx, historyColorAdapter.findFirstColor()) { color ->
                onColorChanged(color)
            }
        }
    }

    private fun postRadiusSliderBackPressure() {
        binding.radiusSlider.removeCallbacks(radiusSliderBackPressure)
        binding.radiusSlider.postDelayed(radiusSliderBackPressure, 100)
    }

    private fun postSizeSliderBackPressure() {
        binding.sizeSlider.removeCallbacks(sizeSliderBackPressure)
        binding.sizeSlider.postDelayed(sizeSliderBackPressure, 100)
    }

    private fun invokeRadiusSliderChanged() {
        // slider里面是0~100的，我们需要的是0~1，所以需要缩小100倍
        val value = binding.radiusSlider.value.progressToValue()
        LAST_RADIUS_VALUE = value
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

    private fun invokeSizeSliderChanged() {
        // slider里面是0~100的，我们需要的是0~1，所以需要缩小100倍
        val value = binding.sizeSlider.value.progressToValue()
        POINT_SIZE = value
        notifyContentChanged()
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
                PigmentTheme.updateSlider(binding.sizeSlider, color)
            }
    }

    abstract class BaseLayer : BitMatrixWriterLayer(), ContentWriterLayer {

        private val radius = Radius()
        private var pointSize = POINT_SIZE
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
            pointSize = POINT_SIZE
            val path = contentPath
            path.reset()
            buildContentPath(path, radius, pointSize)
        }

        abstract fun buildContentPath(path: Path, radius: Radius, pointSize: Float)

        protected fun isInAlignmentPattern(matrix: LQrBitMatrix, x: Int, y: Int): Boolean {
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
            if (!radius.isSame(RADIUS) || pointSize != POINT_SIZE) {
                updateDataPointPath()
            }
        }

    }

}