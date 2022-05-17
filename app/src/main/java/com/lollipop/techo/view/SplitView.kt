package com.lollipop.techo.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.lollipop.techo.data.SplitStyle
import com.lollipop.techo.util.SplitLoader

class SplitView(
    context: Context, attributeSet: AttributeSet?, style: Int
) : AppCompatImageView(context, attributeSet, style) {

    constructor(context: Context, attributeSet: AttributeSet?): this(context, attributeSet, 0)
    constructor(context: Context): this(context, null)

    fun load(splitStyle: SplitStyle) {
        val info = SplitLoader.getInfo(splitStyle)
        TODO()
    }

}