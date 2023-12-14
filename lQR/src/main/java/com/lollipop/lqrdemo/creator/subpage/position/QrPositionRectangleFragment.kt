package com.lollipop.lqrdemo.creator.subpage.position

import android.graphics.Canvas
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.layer.PositionWriterLayer
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustContentFragment
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer

open class QrPositionRectangleFragment : QrPositionRectangleBaseFragment() {

    override fun getWriterLayer(): Class<out QrWriterLayer> {
        return Layer::class.java
    }

    // notifyContentChanged()

    class Layer : BaseLayer() {

        override fun drawPosition(canvas: Canvas) {
            TODO("Not yet implemented")
        }

    }

}