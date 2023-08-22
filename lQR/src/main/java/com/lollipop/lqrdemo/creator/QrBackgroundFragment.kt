package com.lollipop.lqrdemo.creator

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.lollipop.base.util.checkCallback
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.clip.ClipLayout
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.creator.background.BackgroundGravity
import com.lollipop.lqrdemo.creator.background.BackgroundInfo
import com.lollipop.lqrdemo.creator.background.BackgroundStore
import com.lollipop.lqrdemo.databinding.FragmentQrEditBackgroundBinding
import com.lollipop.lqrdemo.writer.background.BackgroundWriterLayer
import com.lollipop.lqrdemo.writer.background.BitmapBackgroundWriterLayer
import com.lollipop.lqrdemo.writer.background.ColorBackgroundWriterLayer
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment

/**
 * 背景的设置
 */
class QrBackgroundFragment : QrBaseSubpageFragment() {

    private val binding: FragmentQrEditBackgroundBinding by lazyBind()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    private var currentMode = Mode.NONE

    private var callback: Callback? = null

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
        updateColorPreview(ColorBackgroundWriterLayer.loadCurrentColor())
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
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
        val lastInfo = BackgroundStore.getByType<BackgroundInfo.Local>()
        if (lastInfo == null) {
            callback?.requestBackgroundPhoto()
            return
        }
        var index = 0
        val current = lastInfo.gravity
        val gravities = ImageGravity.values()
        for (i in gravities.indices) {
            val item = gravities[i]
            if (item.gravity == current) {
                index = i
                break
            }
        }
        index++
        index %= gravities.size
        val newGravity = gravities[index]
        BackgroundStore.set(
            BackgroundInfo.Local(
                lastInfo.file,
                newGravity.gravity,
                lastInfo.corner
            )
        )
        updateImageGravity(newGravity)
        callback?.onBackgroundChanged()
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
        currentMode = mode
        binding.colorModeButton.isStrokeEnable = mode == Mode.COLOR
        binding.imageModeButton.isStrokeEnable = mode == Mode.IMAGE
        binding.noneModeButton.isStrokeEnable = mode == Mode.NONE
    }

    private fun updateImageGravity(gravity: ImageGravity? = null) {
        val imageGravity = if (gravity == null) {
            val imageBgInfo = BackgroundStore.getByType<BackgroundInfo.Local>()
            val backgroundGravity = imageBgInfo?.getGravityOrNull() ?: BackgroundGravity.DEFAULT
            ImageGravity.values().find {
                it.gravity == backgroundGravity
            } ?: ImageGravity.CENTER
        } else {
            gravity
        }
        binding.imageGravityButton.setImageResource(imageGravity.icon)
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
        onBackgroundChanged()
    }

    fun onBackgroundChanged() {
        when (BackgroundStore.get()) {
            is BackgroundInfo.Color -> {
                updateSelectedButton(Mode.COLOR)
            }

            is BackgroundInfo.Local -> {
                updateSelectedButton(Mode.IMAGE)
            }

            BackgroundInfo.None, null -> {
                updateSelectedButton(Mode.NONE)
            }
        }
        val imageBgInfo = BackgroundStore.getByType<BackgroundInfo.Local>()
        if (imageBgInfo == null || !imageBgInfo.file.exists()) {
            Glide.with(this).clear(binding.imageModePreview)
            binding.imageModePreview.setImageDrawable(null)
        } else {
            Glide.with(this)
                .load(imageBgInfo.file)
                .into(binding.imageModePreview)
        }
        updateImageGravity()
        updateColorPreview(ColorBackgroundWriterLayer.loadCurrentColor())
    }

    private fun onModeButtonClick(mode: Mode) {
        val lastMode = currentMode
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
                        PaletteDialog.show(c, ColorBackgroundWriterLayer.loadCurrentColor()) {
                            onColorChanged(it)
                        }
                    }
                }
            }

            Mode.NONE -> {}
        }
    }

    private fun onColorChanged(newColor: Int) {
        updateColorPreview(newColor)
        val lastInfo = BackgroundStore.getByType<BackgroundInfo.Color>()
        BackgroundStore.set(BackgroundInfo.Color(newColor, lastInfo?.corner))
        callback?.onBackgroundChanged()
    }


    private enum class ImageGravity(
        val gravity: BackgroundGravity,
        val icon: Int
    ) {
        LEFT(
            BackgroundGravity.LEFT,
            R.drawable.ic_baseline_keyboard_double_arrow_right_24
        ),
        Top(
            BackgroundGravity.Top,
            R.drawable.ic_baseline_keyboard_double_arrow_down_24
        ),
        RIGHT(
            BackgroundGravity.RIGHT,
            R.drawable.ic_baseline_keyboard_double_arrow_left_24
        ),
        BOTTOM(
            BackgroundGravity.BOTTOM,
            R.drawable.ic_baseline_keyboard_double_arrow_up_24
        ),
        CENTER(
            BackgroundGravity.CENTER,
            R.drawable.ic_baseline_fullscreen_24
        );

        companion object {
            fun find(gravity: BackgroundGravity): ImageGravity {
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

        fun requestBackgroundPhoto()
    }

}