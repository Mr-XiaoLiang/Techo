package com.lollipop.clip

import android.content.Context
import android.graphics.Path
import android.util.AttributeSet

class VectorClipLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ClipLayout(context, attrs) {

    private var vectorInfo: VectorHelper.VectorInfo? = null

    init {
        attrs?.let { a ->
            val typeArray = context.obtainStyledAttributes(a, R.styleable.VectorClipLayout)
            val viewportWidth = typeArray.getInt(R.styleable.VectorClipLayout_viewportWidth, 0)
            val viewportHeight = typeArray.getInt(R.styleable.VectorClipLayout_viewportHeight, 0)
            val pathData = typeArray.getString(R.styleable.VectorClipLayout_pathData) ?: ""
            if (viewportWidth > 0 && viewportHeight > 0 && pathData.isNotEmpty()) {
                vectorInfo = VectorHelper.parse(viewportWidth, viewportHeight, pathData)
            }
            typeArray.recycle()
        }
    }

    override fun rebuildClipPathWithStroke(path: Path) {
        val info = vectorInfo ?: return
        val vWidth = width - strokeWidth
        val vHeight = height - strokeWidth
        val halfStroke = (strokeWidth * 0.5F).toInt()
        info.onBoundsChanged(vWidth, vHeight, halfStroke, halfStroke)
        path.addPath(info.path)
    }

    override fun rebuildClipPathNotStroke(path: Path) {
        val info = vectorInfo ?: return
        info.onBoundsChanged(width, height, paddingLeft, paddingTop)
        path.addPath(info.path)
    }


    fun setVector(vectorData: VectorHelper.VectorData) {
        vectorInfo = VectorHelper.parse(vectorData)
        rebuildStrokePath()
        rebuildDefaultPath()
    }

}