package com.lollipop.browser.main.launcher

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lollipop.base.util.bind
import com.lollipop.base.util.onClick
import com.lollipop.browser.databinding.ItemMainPageLauncherBinding
import java.io.File

class LauncherHolder(
    private val binding: ItemMainPageLauncherBinding,
    private val onClickCallback: (Int) -> Unit,
    private val onLongClickCallback: (Int) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(
            parent: ViewGroup,
            onClickCallback: (Int) -> Unit,
            onLongClickCallback: (Int) -> Unit,
        ): LauncherHolder {
            return LauncherHolder(
                parent.bind(false),
                onClickCallback,
                onLongClickCallback,
            )
        }
    }

    init {
        binding.root.onClick {
            onHolderClick()
        }
        binding.root.setOnLongClickListener {
            onHolderLongClick()
        }
    }

    private fun onHolderClick() {
        onClickCallback(adapterPosition)
    }

    private fun onHolderLongClick(): Boolean {
        onLongClickCallback(adapterPosition)
        return true
    }

    fun bind(info: LauncherInfo) {
        setBackground(binding.backgroundView, info.backgroundColor)
        binding.labelView.text = info.label
        loadOrClear(binding.backgroundView, info.backgroundFile)
        loadOrClear(binding.iconView, info.icon)
    }

    private fun loadOrClear(imageView: ImageView, file: File?) {
        if (file == null) {
            imageView.setImageDrawable(null)
            return
        }
        Glide.with(imageView).load(file).into(imageView)
    }

    private fun setBackground(view: View, colorList: IntArray) {
        val background = view.background
        if (background is BackgroundDrawable) {
            background.onColorChanged(colorList)
            return
        }
        val newBackground = BackgroundDrawable()
        newBackground.onColorChanged(colorList)
        view.background = newBackground
    }

    private class BackgroundDrawable : Drawable() {

        private val colorList = ArrayList<Int>()
        private val paint = Paint()

        fun onColorChanged(color: IntArray) {
            colorList.clear()
            color.forEach {
                colorList.add(it)
            }
            buildShader()
        }

        override fun onBoundsChange(bounds: Rect) {
            super.onBoundsChange(bounds)
            buildShader()
        }

        private fun buildShader() {
            if (bounds.isEmpty) {
                paint.shader = null
                return
            }
            val colorArray = colorList.toIntArray()
            if (colorArray.size > 1) {
                paint.shader = LinearGradient(
                    bounds.left.toFloat(),
                    bounds.top.toFloat(),
                    bounds.right.toFloat(),
                    bounds.bottom.toFloat(),
                    colorArray,
                    null,
                    Shader.TileMode.CLAMP
                )
            } else if (colorArray.isNotEmpty()) {
                paint.color = colorArray[0]
            } else {
                paint.color = Color.BLACK
            }
            invalidateSelf()
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRect(bounds, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        @Deprecated(
            "Deprecated in Java",
            ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat")
        )
        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

    }

}