package com.lollipop.clip

import android.content.Context
import android.graphics.Path
import android.util.AttributeSet

/**
 * 超椭圆的剪裁工具类
 */
class SquircleClipLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ClipLayout(context, attrs) {


    override fun rebuildClipPathWithStroke(path: Path) {
        // TODO
    }

    override fun rebuildClipPathNotStroke(path: Path) {
        // TODO
    }

}