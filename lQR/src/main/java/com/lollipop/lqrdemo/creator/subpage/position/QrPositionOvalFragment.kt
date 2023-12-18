package com.lollipop.lqrdemo.creator.subpage.position

import com.lollipop.lqrdemo.creator.writer.QrWriterLayer

class QrPositionOvalFragment : QrPositionRectangleBaseFragment() {

    override fun getWriterLayer(): Class<out QrWriterLayer> {
        return Layer::class.java
    }

    // notifyContentChanged()

    class Layer : BaseLayer() {

        override val borderRadius: Float
            get() = BORDER_RADIUS_MAX

        override val coreRadius: Float
            get() = CORE_RADIUS_MAX

    }

}