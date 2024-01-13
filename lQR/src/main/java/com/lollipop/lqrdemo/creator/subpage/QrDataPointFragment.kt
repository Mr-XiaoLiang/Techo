package com.lollipop.lqrdemo.creator.subpage

import android.content.Context
import com.lollipop.base.util.checkCallback
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustFragment

/**
 * 数据点的设置
 *
 */
class QrDataPointFragment : StyleAdjustFragment() {

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
        TODO("Not yet implemented")
    }

    override fun notifyContentChanged() {
        TODO("Not yet implemented")
    }

    override fun notifyLayerChanged(layer: Class<out BitMatrixWriterLayer>) {
        TODO("Not yet implemented")
    }

    interface Callback {
        fun onDataPointLayerChanged(layer: Class<out BitMatrixWriterLayer>)

        fun onDataPointStyleChanged()
    }

}