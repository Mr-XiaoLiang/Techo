package com.lollipop.lqrdemo.creator.subpage

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lollipop.base.util.changeAlpha
import com.lollipop.base.util.checkCallback
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.creator.QrCornerSettingDialog
import com.lollipop.lqrdemo.creator.QrCornerSettingHelper
import com.lollipop.lqrdemo.creator.background.BackgroundCorner
import com.lollipop.lqrdemo.databinding.FragmentQrCornerBinding
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentTint

/**
 * 圆角设置的页面，它和背景设置其实是一起的，但是业务上，放不到同一个页面里面
 */
class QrCornerFragment : QrBaseSubpageFragment() {

    private val binding: FragmentQrCornerBinding by lazyBind()

    private val cutModeDrawable by lazy {
        QrCornerSettingHelper.cutModeDrawable()
    }

    private val roundModeDrawable by lazy {
        QrCornerSettingHelper.roundModeDrawable()
    }

    private val squircleModeDrawable by lazy {
        QrCornerSettingHelper.squircleModeDrawable()
    }

    private var callback: Callback? = null

    private var currentMode = Mode.None

    private var radiusLeftTop: BackgroundCorner.Radius = BackgroundCorner.Radius.None
    private var radiusRightTop: BackgroundCorner.Radius = BackgroundCorner.Radius.None
    private var radiusRightBottom: BackgroundCorner.Radius = BackgroundCorner.Radius.None
    private var radiusLeftBottom: BackgroundCorner.Radius = BackgroundCorner.Radius.None

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = checkCallback(context)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
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
        binding.roundModeIcon.setImageDrawable(roundModeDrawable)
        binding.cutModeIcon.setImageDrawable(cutModeDrawable)
        binding.squircleModeIcon.setImageDrawable(squircleModeDrawable)
        updateMode(currentMode)
        binding.noneModeButton.onClick {
            changeMode(Mode.None)
        }
        binding.cutModeButton.onClick {
            changeMode(Mode.Cut)
        }
        binding.roundModeButton.onClick {
            changeMode(Mode.Round)
        }
        binding.squircleModeButton.onClick {
            changeMode(Mode.Squircle)
        }
        binding.leftTopRadius.onClick {
            showRadiusDialog(R.string.corner_left_top, radiusLeftTop) { weight, value ->
                radiusLeftTop = getRadius(weight, value)
            }
        }
        binding.rightTopRadius.onClick {
            showRadiusDialog(R.string.corner_right_top, radiusRightTop) { weight, value ->
                radiusRightTop = getRadius(weight, value)
            }
        }
        binding.rightBottomRadius.onClick {
            showRadiusDialog(R.string.corner_right_bottom, radiusRightBottom) { weight, value ->
                radiusRightBottom = getRadius(weight, value)
            }
        }
        binding.leftBottomRadius.onClick {
            showRadiusDialog(R.string.corner_left_bottom, radiusLeftBottom) { weight, value ->
                radiusLeftBottom = getRadius(weight, value)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val corner = callback?.getCorner() ?: BackgroundCorner.None
        val mode = when (corner) {
            is BackgroundCorner.Cut -> {
                Mode.Cut
            }

            BackgroundCorner.None -> {
                Mode.None
            }

            is BackgroundCorner.Round -> {
                Mode.Round
            }

            is BackgroundCorner.Squircle -> {
                Mode.Squircle
            }
        }
        radiusLeftTop = corner.leftTop
        radiusRightTop = corner.rightTop
        radiusRightBottom = corner.rightBottom
        radiusLeftBottom = corner.leftBottom
        updateMode(mode)
        updateRadius()
    }

    private fun changeMode(mode: Mode) {
        updateMode(mode)
        notifyCornerChanged()
    }

    private fun getRadius(weight: Boolean, value: Float): BackgroundCorner.Radius {
        if (value < 1F) {
            return BackgroundCorner.Radius.None
        }
        return if (weight) {
            BackgroundCorner.Radius.Weight(value)
        } else {
            BackgroundCorner.Radius.Absolute(value)
        }
    }

    private fun showRadiusDialog(
        title: Int,
        radius: BackgroundCorner.Radius,
        callback: QrCornerSettingDialog.Callback
    ) {
        val con = context ?: return
        val value = when (radius) {
            is BackgroundCorner.Radius.Absolute -> {
                radius.value
            }

            BackgroundCorner.Radius.None -> {
                0F
            }

            is BackgroundCorner.Radius.Weight -> {
                radius.value
            }
        }
        QrCornerSettingDialog(
            QrCornerSettingDialog.Option(
                context = con,
                title = con.getString(title),
                weightEnable = radius is BackgroundCorner.Radius.Weight,
                value = value,
                callback = callback
            )
        ).show()
    }

    private fun notifyCornerChanged() {
        val corner = when (currentMode) {
            Mode.Cut -> {
                BackgroundCorner.Cut(
                    leftTop = radiusLeftTop,
                    rightTop = radiusRightTop,
                    rightBottom = radiusRightBottom,
                    leftBottom = radiusLeftBottom
                )
            }

            Mode.Round -> {
                BackgroundCorner.Round(
                    leftTop = radiusLeftTop,
                    rightTop = radiusRightTop,
                    rightBottom = radiusRightBottom,
                    leftBottom = radiusLeftBottom
                )
            }

            Mode.Squircle -> {
                BackgroundCorner.Squircle(
                    leftTop = radiusLeftTop,
                    rightTop = radiusRightTop,
                    rightBottom = radiusRightBottom,
                    leftBottom = radiusLeftBottom
                )
            }

            Mode.None -> {
                BackgroundCorner.None
            }
        }
        callback?.onCornerChanged(corner)
    }

    private fun updateRadius() {
        updateRadius(binding.leftTopRadius, radiusLeftTop)
        updateRadius(binding.rightTopRadius, radiusRightTop)
        updateRadius(binding.rightBottomRadius, radiusRightBottom)
        updateRadius(binding.leftBottomRadius, radiusLeftBottom)
    }

    private fun updateRadius(textView: TextView, radius: BackgroundCorner.Radius) {
        val value = when (radius) {
            is BackgroundCorner.Radius.Absolute -> {
                "${radius.value.toInt()}dp"
            }

            BackgroundCorner.Radius.None -> {
                textView.context.getString(R.string.radius_none)
            }

            is BackgroundCorner.Radius.Weight -> {
                "${radius.value.toInt()}%"
            }
        }
        textView.text = value
    }

    private fun updateMode(mode: Mode) {
        currentMode = mode
        binding.noneModeIcon.isSelected = mode == Mode.None
        binding.roundModeIcon.isSelected = mode == Mode.Round
        binding.cutModeIcon.isSelected = mode == Mode.Cut
        binding.squircleModeIcon.isSelected = mode == Mode.Squircle
    }

    private fun getModeBtnTintList(pigment: Pigment): ColorStateList {
        return PigmentTint.getSelectStateList(
            pigment.primaryColor,
            pigment.primaryColor.changeAlpha(0.5f)
        )
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        val btnTintList = getModeBtnTintList(pigment)
        binding.noneModeIcon.imageTintList = btnTintList
        binding.roundModeIcon.imageTintList = btnTintList
        binding.cutModeIcon.imageTintList = btnTintList
        binding.squircleModeIcon.imageTintList = btnTintList
        binding.modeBar.setBackgroundColor(pigment.primaryColor.changeAlpha(0.3F))
    }

    private enum class Mode {
        Cut,
        Round,
        Squircle,
        None
    }

    interface Callback {
        fun getCorner(): BackgroundCorner

        fun onCornerChanged(corner: BackgroundCorner)
    }


}