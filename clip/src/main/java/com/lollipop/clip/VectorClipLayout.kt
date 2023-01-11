package com.lollipop.clip

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout

class VectorClipLayout(
    context: Context, attrs: AttributeSet?, style: Int
) : FrameLayout(context, attrs, style) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

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

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        vectorInfo?.onBoundsChanged(width, height, paddingLeft, paddingTop)
    }

    fun setVector(vectorData: VectorHelper.VectorData) {
        vectorInfo = VectorHelper.parse(vectorData)
        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        val vector = vectorInfo
        if (vector == null || vector.path.isEmpty) {
            super.onDraw(canvas)
            return
        }
        val saveCount = canvas.save()
        canvas.clipPath(vector.path)
        super.onDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val vector = vectorInfo
        if (vector == null || vector.path.isEmpty) {
            super.dispatchDraw(canvas)
            return
        }
        val saveCount = canvas.save()
        canvas.clipPath(vector.path)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

}