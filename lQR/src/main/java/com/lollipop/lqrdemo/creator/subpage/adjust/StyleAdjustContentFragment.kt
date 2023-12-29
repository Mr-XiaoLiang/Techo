package com.lollipop.lqrdemo.creator.subpage.adjust

import android.content.Context
import com.lollipop.base.util.checkCallback
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.creator.layer.BitMatrixWriterLayer
import com.lollipop.lqrdemo.creator.layer.DefaultWriterLayer
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer

open class StyleAdjustContentFragment : BaseFragment() {

    private var callback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = checkCallback(context)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    protected fun notifyContentChanged() {
        callback?.notifyContentChanged()
    }

    open fun getWriterLayer(): Class<out BitMatrixWriterLayer> {
        return DefaultWriterLayer::class.java
    }

    override fun onResume() {
        super.onResume()
        notifyLayerChanged(getWriterLayer())
    }

    protected fun notifyLayerChanged(layer: Class<out BitMatrixWriterLayer>) {
        callback?.notifyLayerChanged(layer)
    }

    interface Callback {

        fun notifyContentChanged()

        fun notifyLayerChanged(layer: Class<out BitMatrixWriterLayer>)

    }

}