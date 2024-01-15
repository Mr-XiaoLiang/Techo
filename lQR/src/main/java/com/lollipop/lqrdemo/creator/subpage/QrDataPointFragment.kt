package com.lollipop.lqrdemo.creator.subpage

import android.content.Context
import com.lollipop.base.util.checkCallback
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustFragment
import com.lollipop.lqrdemo.creator.subpage.datapoint.QrDataPointRectangleFragment

/**
 * 数据点的设置
 *
 */
class QrDataPointFragment : StyleAdjustFragment() {

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
                panel = QrDataPointRectangleFragment::class.java,
                key = KEY_RECTANGLE
            )
        )
    }

    override fun notifyContentChanged() {
        callback?.onDataPointStyleChanged()
    }

    override fun notifyLayerChanged(layer: Class<out BitMatrixWriterLayer>) {
        callback?.onDataPointLayerChanged(layer)
    }

    interface Callback {
        fun onDataPointLayerChanged(layer: Class<out BitMatrixWriterLayer>)

        fun onDataPointStyleChanged()
    }

}