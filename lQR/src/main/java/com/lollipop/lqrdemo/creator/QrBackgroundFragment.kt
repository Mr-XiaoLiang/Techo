package com.lollipop.lqrdemo.creator

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.lollipop.base.util.checkCallback
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.clip.ClipLayout
import com.lollipop.filechooser.FileChooseLauncher
import com.lollipop.filechooser.FileChooseResult
import com.lollipop.filechooser.FileChooser
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.databinding.FragmentQrEditBackgroundBinding
import com.lollipop.lqrdemo.writer.background.BackgroundWriterLayer
import com.lollipop.lqrdemo.writer.background.BitmapBackgroundWriterLayer
import com.lollipop.lqrdemo.writer.background.ColorBackgroundWriterLayer
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment

/**
 * 背景的设置
 */
class QrBackgroundFragment : BaseFragment() {

    private val binding: FragmentQrEditBackgroundBinding by lazyBind()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    private var color = Color.GREEN
    private var currentMode = Mode.NONE

    private var callback: Callback? = null

    private var currentGravity = ImageGravity.CENTER

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.colorModeButton.onClick {
            onModeButtonClick(Mode.COLOR)
        }
        binding.imageGravityButton.onClick {
            nextImageGravity()
        }
        binding.imageModeButton.onClick {
            onModeButtonClick(Mode.IMAGE)
        }
        binding.noneModeButton.onClick {
            onModeButtonClick(Mode.NONE)
        }
        updateSelectedButton(currentMode)
        color = ColorBackgroundWriterLayer.color
        updateColorPreview(color)
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.extreme)
        setModePreviewBackground(
            pigment,
            binding.imageModePreview,
            binding.colorModePreview,
            binding.noneModePreview
        )

        setModeStrokeColor(
            pigment,
            binding.imageModeButton,
            binding.colorModeButton,
            binding.noneModeButton,
        )

        setModeIconColor(
            pigment,
            binding.colorModeIcon,
            binding.imageModeIcon,
            binding.noneModeIcon,
            binding.imageGravityButton
        )
        binding.imageGravityButton.setBackgroundColor(pigment.extreme)
    }

    private fun nextImageGravity() {
        var index = 0
        val current = currentGravity
        val gravities = ImageGravity.values()
        gravities.forEachIndexed { i, item ->
            if (item == current) {
                index = i
            }
        }
        index++
        index %= gravities.size
        currentGravity = gravities[index]
        updateImageGravity()
        callback?.onBackgroundGravityChanged(currentGravity.gravity)
    }

    private fun setModePreviewBackground(pigment: Pigment, vararg views: View) {
        val buttonBackground = BlendMode.blend(pigment.extreme, pigment.primaryColor)
        views.forEach {
            it.setBackgroundColor(buttonBackground)
        }
    }

    private fun setModeStrokeColor(pigment: Pigment, vararg views: ClipLayout) {
        views.forEach {
            it.setStrokeColor(pigment.onExtremeBody)
        }
    }

    private fun setModeIconColor(pigment: Pigment, vararg views: ImageView) {
        views.forEach {
            it.imageTintList = ColorStateList.valueOf(pigment.onExtremeBody)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = checkCallback(context)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    private fun updateSelectedButton(mode: Mode) {
        binding.colorModeButton.isStrokeEnable = mode == Mode.COLOR
        binding.imageModeButton.isStrokeEnable = mode == Mode.IMAGE
        binding.noneModeButton.isStrokeEnable = mode == Mode.NONE
    }

    private fun updateImageGravity() {
        binding.imageGravityButton.setImageResource(currentGravity.icon)
    }

    private fun updateColorPreview(newColor: Int) {
        val drawable = binding.colorModePreview.drawable
        if (drawable is ColorDrawable) {
            drawable.color = newColor
        } else {
            val newDrawable = ColorDrawable(newColor)
            binding.colorModePreview.setImageDrawable(newDrawable)
        }
    }

    override fun onResume() {
        super.onResume()
        onBackgroundPhotoChanged()
    }

    fun onBackgroundPhotoChanged() {
        val background = callback?.getCurrentBackground() ?: ""
        if (background.isEmpty()) {
            Glide.with(this).clear(binding.imageModePreview)
            binding.imageModePreview.setImageDrawable(null)
        } else {
            Glide.with(this)
                .load(background)
                .into(binding.imageModePreview)
        }
        currentGravity = ImageGravity.find(
            callback?.getCurrentBackgroundGravity()
                ?: BitmapBackgroundWriterLayer.Gravity.CENTER
        )
        updateImageGravity()
    }

    private fun onModeButtonClick(mode: Mode) {
        val lastMode = currentMode
        currentMode = mode
        updateSelectedButton(mode)
        callback?.onBackgroundModeChanged(mode.modeLayer)
        when (mode) {
            Mode.IMAGE -> {
                if (lastMode == mode) {
                    callback?.requestBackgroundPhoto()
                }
            }

            Mode.COLOR -> {
                if (lastMode == mode) {
                    context?.let { c ->
                        PaletteDialog.show(c, color) {
                            onColorChanged(it)
                        }
                    }
                }
            }

            Mode.NONE -> {}
        }
    }

    private fun onColorChanged(newColor: Int) {
        color = newColor
        updateColorPreview(color)
        ColorBackgroundWriterLayer.color = color
        callback?.onBackgroundChanged()
    }


    private enum class ImageGravity(
        val gravity: BitmapBackgroundWriterLayer.Gravity,
        val icon: Int
    ) {
        LEFT(
            BitmapBackgroundWriterLayer.Gravity.LEFT,
            R.drawable.ic_baseline_keyboard_double_arrow_right_24
        ),
        Top(
            BitmapBackgroundWriterLayer.Gravity.Top,
            R.drawable.ic_baseline_keyboard_double_arrow_down_24
        ),
        RIGHT(
            BitmapBackgroundWriterLayer.Gravity.RIGHT,
            R.drawable.ic_baseline_keyboard_double_arrow_left_24
        ),
        BOTTOM(
            BitmapBackgroundWriterLayer.Gravity.BOTTOM,
            R.drawable.ic_baseline_keyboard_double_arrow_up_24
        ),
        CENTER(
            BitmapBackgroundWriterLayer.Gravity.CENTER,
            R.drawable.ic_baseline_fullscreen_24
        );

        companion object {
            fun find(gravity: BitmapBackgroundWriterLayer.Gravity): ImageGravity {
                return values().find { it.gravity == gravity } ?: CENTER
            }
        }

    }

    private enum class Mode(
        val modeLayer: Class<out BackgroundWriterLayer>?
    ) {

        NONE(null),
        IMAGE(BitmapBackgroundWriterLayer::class.java),
        COLOR(ColorBackgroundWriterLayer::class.java)

    }

    interface Callback {
        fun onBackgroundModeChanged(layer: Class<out BackgroundWriterLayer>?)

        fun onBackgroundChanged()

        fun getCurrentBackground(): String

        fun getCurrentBackgroundGravity(): BitmapBackgroundWriterLayer.Gravity

        fun onBackgroundGravityChanged(gravity: BitmapBackgroundWriterLayer.Gravity)

        fun requestBackgroundPhoto()
    }

}