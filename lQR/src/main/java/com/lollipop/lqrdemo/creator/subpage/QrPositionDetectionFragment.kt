package com.lollipop.lqrdemo.creator.subpage

import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustFragment
import com.lollipop.lqrdemo.creator.subpage.position.QrPositionOvalFragment
import com.lollipop.lqrdemo.creator.subpage.position.QrPositionRectangleFragment
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer

/**
 * 主要定位点的设置
 */
class QrPositionDetectionFragment : StyleAdjustFragment() {

    companion object {
        private const val KEY_RECTANGLE = "Rectangle"
        private const val KEY_OVAL = "Oval"
    }

    override fun getContentTabs(): List<TabInfo> {
        return listOf(
            TabInfo(
                tabIcon = 0,
                panel = QrPositionRectangleFragment::class.java,
                key = KEY_RECTANGLE
            ),
            TabInfo(
                tabIcon = 0,
                panel = QrPositionOvalFragment::class.java,
                key = KEY_OVAL
            ),
        )
    }

    override fun notifyContentChanged() {
        TODO("Not yet implemented")
    }

    override fun notifyLayerChanged(layer: Class<out QrWriterLayer>) {
        TODO("Not yet implemented")
    }

//    Horizontal
//    Vertical
//    Crossover


}

