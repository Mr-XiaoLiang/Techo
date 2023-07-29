package com.lollipop.lqrdemo.creator

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.Gravity
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.base.util.parseColor
import com.lollipop.lqrdemo.databinding.DialogPaletteBinding
import com.lollipop.palette.ColorWheelView
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.PigmentWallpaperCenter

class PaletteDialog(
    context: Context,
    color: Int,
    private val selectedColorCallback: (Int) -> Unit
) : Dialog(context), ColorWheelView.OnColorChangedListener {

    companion object {
        fun show(context: Context, color: Int, callback: (Int) -> Unit) {
            PaletteDialog(context, color, callback).show()
        }
    }

    private val binding: DialogPaletteBinding by lazyBind()

    private var currentColor = color

    private var colorTextWatchLock = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            it.setGravity(Gravity.CENTER)
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.colorWheelView.setOnColorChangedListener(this)
        binding.colorInputView.doAfterTextChanged {
            onColorInputTextChanged(it)
        }

        binding.confirmButtonText.onClick {
            onConfirmClick()
        }

        getThemeColor { fg, bg ->
            binding.numberIconView.imageTintList = ColorStateList.valueOf(fg)
            binding.contentGroup.setBackgroundColor(bg)
            binding.colorWheelView.setValueSlideBarColor(fg)
            binding.colorWheelView.setAnchorStrokeColor(bg)
        }
        resetColor(currentColor)
    }

    private fun getThemeColor(callback: (fg: Int, bg: Int) -> Unit) {
        val pigment = PigmentWallpaperCenter.pigment
        if (pigment == null) {
            callback(Color.WHITE, Color.BLACK)
            return
        }
        callback(
            pigment.backgroundColor,
            BlendMode.blend(pigment.onBackgroundTitle, pigment.primaryColor)
        )
    }

    override fun onColorChanged(h: Float, s: Float, v: Float, a: Float) {
        val color = Color.HSVToColor(floatArrayOf(h, s, v))
        onColorChanged(color, true)
    }

    private fun resetColor(color: Int) {
        val newColor = color.or(0xFF000000.toInt())
        onColorChanged(newColor, false)
    }

    private fun onColorChanged(color: Int, fromUser: Boolean) {
        currentColor = color
        binding.confirmButtonText.setBackgroundColor(color)
        binding.confirmButtonText.setTextColor(BlendMode.titleOnColor(color))
        if (!fromUser) {
            colorTextWatchLock = true
        }
        binding.colorInputView.setText(formatColorValue(color))
        colorTextWatchLock = false
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

    private fun onColorInputTextChanged(value: Editable?) {
        if (colorTextWatchLock) {
            return
        }
        value ?: return
        if (value.length != 3 && value.length != 6) {
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