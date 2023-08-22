package com.lollipop.lqrdemo.writer.background

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.ColorInt
import com.lollipop.lqrdemo.creator.background.BackgroundInfo
import com.lollipop.lqrdemo.creator.background.BackgroundStore

class ColorBackgroundWriterLayer : BackgroundWriterLayer() {

    companion object {
        fun loadCurrentColor(): Int {
            return BackgroundStore.getByType<BackgroundInfo.Color>()?.color ?: Color.WHITE
        }
    }

    private var currentColor = 0
    private var customColor = false

    private var color: Int = Color.WHITE

    init {
        loadColor()
    }

    private val paint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    override fun onUpdateResource() {
        super.onUpdateResource()
        loadColor()
    }

    private fun loadColor() {
        color = loadCurrentColor()
        onResourceReady()
    }

    override fun draw(canvas: Canvas) {
        val c = if (customColor) {
            currentColor
        } else {
            color
        }
        if (clipPath.isEmpty) {
            canvas.drawColor(c)
        } else {
            paint.color = c
            canvas.drawPath(clipPath, paint)
        }
    }


    fun customColor(@ColorInt c: Int) {
        this.currentColor = c
        this.customColor = true
    }

    fun clearCustomColor() {
        this.customColor = false
        this.currentColor = 0
    }

}