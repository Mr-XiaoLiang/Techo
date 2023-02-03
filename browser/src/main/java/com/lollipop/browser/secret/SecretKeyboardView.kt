package com.lollipop.browser.secret

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lollipop.browser.R

class SecretKeyboardView @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null, style: Int = 0
) : ViewGroup(context, attributeSet, style) {

    private var keyButtonProvider: KeyButtonProvider? = null

    var spaceHorizontal = 0
        set(value) {
            field = value
            requestLayout()
        }
    var spaceVertical = 0
        set(value) {
            field = value
            requestLayout()
        }

    init {
        if (attributeSet != null) {
            val typeArray =
                context.obtainStyledAttributes(attributeSet, R.styleable.SecretKeyboardView)
            spaceHorizontal = typeArray.getDimensionPixelSize(
                R.styleable.SecretKeyboardView_buttonSpaceHorizontal,
                0
            )
            spaceVertical = typeArray.getDimensionPixelSize(
                R.styleable.SecretKeyboardView_buttonSpaceVertical,
                0
            )
            typeArray.recycle()
        }
        if (isInEditMode) {
            setProvider(EditModeProvider())
        }
    }

    fun setProvider(provider: KeyButtonProvider?) {
        keyButtonProvider = provider
        onProviderChanged()
    }

    private fun onProviderChanged() {
        val provider = keyButtonProvider
        removeAllViews()
        if (provider == null) {
            return
        }
        Key.values().forEach {
            val button = provider.createButton(this, it)
            addView(button)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (keyButtonProvider == null || childCount < Key.values().size) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        forEachKeySize(widthSize, heightSize) { key, itemWidth, itemHeight, x, y ->
            getChildAt(key.ordinal)?.measure(
                MeasureSpec.makeMeasureSpec(itemWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY),
            )
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        forEachKeySize(width, height) { key, itemWidth, itemHeight, x, y ->
            getChildAt(key.ordinal)?.layout(x, y, x + itemWidth, y + itemHeight)
        }
    }

    private fun forEachKeySize(
        width: Int,
        height: Int,
        outCallback: (key: Key, itemWidth: Int, itemHeight: Int, x: Int, y: Int) -> Unit
    ) {
        val left = paddingLeft
        val top = paddingTop
        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        val spaceH = spaceHorizontal
        val spaceV = spaceVertical
        val stepX = (contentWidth + spaceH) * 1F / 3
        val stepY = (contentHeight + spaceV) * 1F / 4
        val buttonWidth = stepX - spaceH
        val buttonHeight = stepY - spaceV

        Key.values().forEach {
            val itemWidth = (it.spanX * stepX - spaceH).toInt()
            val itemHeight = (it.spanY * stepY - spaceV).toInt()
            val x = (it.x * stepX).toInt() + left
            val y = (it.y * stepY).toInt() + top
            outCallback(it, itemWidth, itemHeight, x, y)
        }

    }

    interface KeyButtonProvider {
        fun createButton(parent: SecretKeyboardView, key: Key): View
    }

    private class EditModeProvider : KeyButtonProvider {
        override fun createButton(parent: SecretKeyboardView, key: Key): View {
            return TextView(parent.context).apply {
                text = "${key.ordinal}"
                gravity = Gravity.CENTER
                setBackgroundColor(Color.RED)
            }
        }
    }

    enum class Key(
        val spanX: Int,
        val spanY: Int,
        val x: Int,
        val y: Int
    ) {
        NUMBER_0(1, 1, 0, 0),
        NUMBER_1(1, 1, 0, 1),
        NUMBER_2(1, 1, 1, 1),
        NUMBER_3(1, 1, 2, 1),
        NUMBER_4(1, 1, 0, 2),
        NUMBER_5(1, 1, 1, 2),
        NUMBER_6(1, 1, 2, 2),
        NUMBER_7(1, 1, 0, 3),
        NUMBER_8(1, 1, 1, 3),
        NUMBER_9(1, 1, 2, 3),
        SUBMIT(2, 1, 1, 0),
    }

}