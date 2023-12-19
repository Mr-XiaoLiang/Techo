package com.lollipop.lqrdemo.creator.subpage.position

import com.lollipop.lqrdemo.creator.writer.QrWriterLayer

open class QrPositionRectangleFragment : QrPositionRectangleBaseFragment() {

    override fun getWriterLayer(): Class<out QrWriterLayer> {
        return Layer::class.java
    }

    // notifyContentChanged()

    class Layer : BaseLayer()

}