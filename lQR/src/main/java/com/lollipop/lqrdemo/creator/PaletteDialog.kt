package com.lollipop.lqrdemo.creator

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onActionDone
import com.lollipop.base.util.onClick
import com.lollipop.base.util.parseColor
import com.lollipop.lqrdemo.base.BaseCenterDialog
import com.lollipop.lqrdemo.databinding.DialogPaletteBinding
import com.lollipop.palette.ColorWheelView
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment

class PaletteDialog(
    context: Context,
    color: Int,
    private val selectedColorCallback: (Int) -> Unit
) : BaseCenterDialog(context), ColorWheelView.OnColorChangedListener {

    companion object {
        fun show(context: Context, color: Int, callback: (Int) -> Unit) {
            PaletteDialog(context, color, callback).show()
        }
    }

    private val binding: DialogPaletteBinding by lazyBind()

    private var currentColor = color

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.colorWheelView.setOnColorChangedListener(this)
        binding.colorInputView.onActionDone {
            onColorInputTextChanged(binding.colorInputView.text ?: "")
        }

        binding.confirmButtonText.onClick {
            onConfirmClick()
        }

        resetColor(currentColor)
    }

    override fun onCreateDialogView(): View {
        return binding.root
    }

    override fun onThemeChanged(pigment: Pigment?, fg: Int, bg: Int) {
        super.onThemeChanged(pigment, fg, bg)
        binding.numberIconView.imageTintList = ColorStateList.valueOf(fg)
        binding.colorInputView.setTextColor(fg)
        binding.contentGroup.setBackgroundColor(bg)
        binding.colorWheelView.setValueSlideBarColor(fg)
        binding.colorWheelView.setAnchorStrokeColor(bg)
    }

    override fun onColorChanged(h: Float, s: Float, v: Float, a: Float) {
        val color = Color.HSVToColor(floatArrayOf(h, s, v))
        onColorChanged(color)
    }

    private fun resetColor(color: Int) {
        val newColor = color.or(0xFF000000.toInt())
        onColorChanged(newColor)
        binding.colorWheelView.reset(color)
    }

    private fun onColorChanged(color: Int) {
        currentColor = color
        binding.confirmButtonText.setBackgroundColor(color)
        binding.confirmButtonText.setTextColor(BlendMode.titleOnColor(color))
        val colorValue = formatColorValue(color)
        binding.colorInputView.setText(colorValue)
        if (binding.colorInputView.hasFocus()) {
            binding.colorInputView.setSelection(colorValue.length)
        }
    }

    private fun formatColorValue(color: Int): String {
        val builder = StringBuilder()

        val red = Color.red(color)
        if (red < 0x10) {
            builder.append("0")
        }
        builder.append(red.toString(16))

        val green = Color.green(color)
        if (green < 0x10) {
            builder.append("0")
        }
        builder.append(green.toString(16))

        val blue = Color.blue(color)
        if (blue < 0x10) {
            builder.append("0")
        }
        builder.append(blue.toString(16))

        return builder.toString().uppercase()
    }

    private fun onColorInputTextChanged(value: CharSequence) {
        if (value.length < 6) {
            return
        }
        try {
            val color = value.toString().parseColor()
            resetColor(color)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun onConfirmClick() {
        selectedColorCallback(currentColor)
        dismiss()
    }

}