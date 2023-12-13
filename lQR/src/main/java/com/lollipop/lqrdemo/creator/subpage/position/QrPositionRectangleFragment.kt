package com.lollipop.lqrdemo.creator.subpage.position

import android.graphics.Canvas
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.layer.PositionWriterLayer
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustContentFragment
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer

class QrPositionRectangleFragment : StyleAdjustContentFragment() {

    override fun getWriterLayer(): Class<out QrWriterLayer> {
        return Layer::class.java
    }

    // notifyContentChanged()

    class Layer : BitMatrixWriterLayer(), PositionWriterLayer {
        override fun drawPosition(canvas: Canvas) {
            TODO("Not yet implemented")
        }

    }

}