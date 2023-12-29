package com.lollipop.lqrdemo.creator.subpage

import android.content.Context
import com.lollipop.base.util.checkCallback
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustFragment
import com.lollipop.lqrdemo.creator.subpage.position.QrPositionRectangleFragment
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer

/**
 * 主要定位点的设置
 */
class QrPositionDetectionFragment : StyleAdjustFragment() {

    companion object {
        private const val KEY_RECTANGLE = "Rectangle"
    }

    private var callback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = checkCallback(context)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun getContentTabs(): List<TabInfo> {
        return listOf(
            TabInfo(
                tabIcon = 0,
                panel = QrPositionRectangleFragment::class.java,
                key = KEY_RECTANGLE,
            ),
        )
    }

    override fun notifyContentChanged() {
        callback?.onPositionDetectionContentChanged()
    }

    override fun notifyLayerChanged(layer: Class<out BitMatrixWriterLayer>) {
        callback?.onPositionDetectionLayerChanged(layer)
    }

//    Horizontal
//    Vertical
//    Crossover

    interface Callback {
        fun onPositionDetectionLayerChanged(layer: Class<out BitMatrixWriterLayer>)

        fun onPositionDetectionContentChanged()
    }

}

