package com.lollipop.lqrdemo.creator.subpage.position

import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.layer.PositionWriterLayer
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustContentFragment

open class QrPositionRectangleBaseFragment : StyleAdjustContentFragment() {

    companion object {

        var BORDER_RADIUS = 0F
        var CORE_RADIUS = 0F

        const val BORDER_RADIUS_MAX = 1F
        const val CORE_RADIUS_MAX = 1F

        const val BORDER_RADIUS_MIN = 0F
        const val CORE_RADIUS_MIN = 0F

    }

    // notifyContentChanged()

    abstract class BaseLayer : BitMatrixWriterLayer(), PositionWriterLayer {

        open val borderRadius: Float
            get() {
                return BORDER_RADIUS
            }
        open val coreRadius: Float
            get() {
                return CORE_RADIUS
            }

    }

}