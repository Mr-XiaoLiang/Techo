package com.lollipop.lqrdemo.creator.subpage.position

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
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

        var BORDER_COLOR = Color.BLACK
        var CORE_COLOR = Color.BLACK

    }

    // notifyContentChanged()

    abstract class BaseLayer : BitMatrixWriterLayer(), PositionWriterLayer {

        @get:FloatRange(from = BORDER_RADIUS_MIN.toDouble(), to = BORDER_RADIUS_MAX.toDouble())
        open val borderRadius: Float
            get() {
                return BORDER_RADIUS
            }

        @get:FloatRange(from = CORE_RADIUS_MIN.toDouble(), to = CORE_RADIUS_MAX.toDouble())
        open val coreRadius: Float
            get() {
                return CORE_RADIUS
            }

        @get:ColorInt
        open val borderColor: Int
            get() {
                return BORDER_COLOR
            }

        @get:ColorInt
        open val coreColor: Int
            get() {
                return CORE_COLOR
            }

    }

}