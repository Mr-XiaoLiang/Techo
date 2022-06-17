package com.lollipop.techo.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.Selection
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText

class SelectTextView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : AppCompatEditText(context, attributeSet, style) {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    private var selectedOffset = 0

    override fun onCreateContextMenu(menu: ContextMenu?) {
        //不做任何处理，为了阻止长按的时候弹出上下文菜单
    }

    override fun getDefaultEditable(): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val line = layout.getLineForVertical((scrollY + event.y).toInt())
                selectedOffset = layout.getOffsetForHorizontal(line, event.x)
                Selection.setSelection(editableText, selectedOffset)
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                val line = layout.getLineForVertical((scrollY + event.y).toInt())
                val curOff: Int = layout.getOffsetForHorizontal(line, event.x)
                Selection.setSelection(editableText, selectedOffset, curOff)
            }
        }
        return true
    }

}