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
import com.lollipop.lqrdemo.R
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

    private val radiusArray = Array<BackgroundCorner.Radius>(4) { BackgroundCorner.Radius.None }

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
//        binding.
        binding.roundModeIcon.setImageDrawable(roundModeDrawable)
        binding.cutModeIcon.setImageDrawable(cutModeDrawable)
        binding.squircleModeIcon.setImageDrawable(squircleModeDrawable)
        updateMode(currentMode)
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
        radiusArray[0] = corner.leftTop
        radiusArray[1] = corner.rightTop
        radiusArray[2] = corner.rightBottom
        radiusArray[3] = corner.leftBottom
        updateMode(mode)
        updateRadius()
    }

    private fun updateRadius() {
        updateRadius(binding.leftTopRadius, radiusArray[0])
        updateRadius(binding.rightTopRadius, radiusArray[1])
        updateRadius(binding.rightBottomRadius, radiusArray[2])
        updateRadius(binding.leftBottomRadius, radiusArray[3])
    }

    private fun updateRadius(textView: TextView, radius: BackgroundCorner.Radius) {
        val value = when (radius) {
            is BackgroundCorner.Radius.Absolute -> {
                textView.context.getString(R.string.radius_absolute, radius.value.toInt())
            }

            BackgroundCorner.Radius.None -> {
                textView.context.getString(R.string.radius_none)
            }

            is BackgroundCorner.Radius.Weight -> {
                textView.context.getString(R.string.radius_percentage, radius.value.toInt())
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