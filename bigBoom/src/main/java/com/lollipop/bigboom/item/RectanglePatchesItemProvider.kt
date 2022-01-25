package com.lollipop.bigboom.item

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.bigboom.PatchesHolder
import com.lollipop.bigboom.PatchesItemProvider
import com.lollipop.bigboom.PatchesStatus

/**
 * 矩形的碎片内容提供者
 */
class RectanglePatchesItemProvider : PatchesItemProvider {

    var selectedColor = Color.BLACK
    var defaultColor = Color.TRANSPARENT
    var selectedTextColor = Color.WHITE
    var defaultTextColor = Color.BLACK
    var margin = Rect()
    var padding = Rect()
    var radius = 0F
    var textSize = 14F

    override fun createHolder(parent: ViewGroup): PatchesHolder {
        return RectangleHolder.create(
            parent,
            Option(
                selectedColor,
                defaultColor,
                selectedTextColor,
                defaultTextColor,
                margin,
                padding,
                radius,
                textSize
            )
        )
    }

    private class Option(
        val selectedColor: Int,
        val defaultColor: Int,
        val selectedTextColor: Int,
        val defaultTextColor: Int,
        val margin: Rect,
        val padding: Rect,
        val radius: Float,
        val textSize: Float,
    )

    private class RectangleHolder private constructor(
        private val view: RectangleView,
        private val option: Option
    ) : PatchesHolder(view) {

        companion object {
            fun create(parent: ViewGroup, option: Option): RectangleHolder {
                return RectangleHolder(RectangleView(parent.context), option)
            }
        }

        init {
            view.apply {
                clipToOutline = true
                outlineProvider = RectangleOutlineProvider(option.radius)
                setRectangleMargin(option.margin)
                setRectanglePadding(option.padding)
                setTextSize(option.textSize)

            }
        }

        override fun bind(value: String, status: PatchesStatus) {
            view.setText(value)
            updateByStatus(status.isSelected)
        }

        private fun updateByStatus(isSelected: Boolean) {
            if (isSelected) {
                view.setItemColor(option.selectedColor)
                view.setTextColor(option.selectedTextColor)
            } else {
                view.setItemColor(option.defaultColor)
                view.setTextColor(option.defaultTextColor)
            }
        }
    }

    private class RectangleView(context: Context) : FrameLayout(context) {

        private val valueView = TextView(context)

        init {
            addView(valueView)
        }

        fun setRectangleMargin(rect: Rect) {
            setPadding(rect.left, rect.top, rect.right, rect.bottom)
        }

        fun setRectanglePadding(rect: Rect) {
            valueView.setPadding(rect.left, rect.top, rect.right, rect.bottom)
        }

        fun setTextSize(float: Float) {
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_PX, float)
        }

        fun setItemColor(@ColorInt color: Int) {
            valueView.setBackgroundColor(color)
        }

        fun setTextColor(@ColorInt color: Int) {
            valueView.setTextColor(color)
        }

        fun setText(value: String) {
            valueView.text = value
        }

    }

    private class RectangleOutlineProvider(private val radius: Float) : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            view ?: return
            outline ?: return
            outline.setRoundRect(
                view.paddingLeft,
                view.paddingTop,
                view.width - view.paddingRight,
                view.height - view.paddingBottom,
                radius
            )
        }
    }

}