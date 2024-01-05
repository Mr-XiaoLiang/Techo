package com.lollipop.lqrdemo.creator.subpage

import android.content.Context
import com.lollipop.base.util.checkCallback
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustFragment
import com.lollipop.lqrdemo.creator.subpage.alignment.QrAlignmentRectangleFragment

/**
 * 辅助定位点的设置
 */
class QrAlignmentFragment : StyleAdjustFragment() {

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
                panel = QrAlignmentRectangleFragment::class.java,
                key = KEY_RECTANGLE,
            ),
        )
    }

    override fun notifyContentChanged() {
        callback?.onAlignmentContentChanged()
    }

    override fun notifyLayerChanged(layer: Class<out BitMatrixWriterLayer>) {
        callback?.onAlignmentLayerChanged(layer)
    }

    interface Callback {
        fun onAlignmentLayerChanged(layer: Class<out BitMatrixWriterLayer>)

        fun onAlignmentContentChanged()
    }
}