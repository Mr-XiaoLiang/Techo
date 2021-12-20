package com.lollipop.techo.edit

import android.view.View
import android.view.ViewGroup
import com.lollipop.techo.data.BaseTechoItem

/**
 * @author lollipop
 * @date 2021/12/18 20:15
 */
abstract class EditDelegate {

    abstract fun isSupport(info: BaseTechoItem): Boolean

    private var controller: PanelController? = null

    private var panelView: View? = null

    fun setController(controller: PanelController?) {
        this.controller = controller
    }

    fun open(info: BaseTechoItem) {
        panelView?.post {
            onOpen(info)
            doAnimation(true)
        }
    }

    fun close() {
        panelView?.post {
            onClose()
            doAnimation(false)
        }
    }

    fun getPanelView(container: ViewGroup): View {
        panelView?.let {
            return it
        }
        val newView = onCreateView(container)
        this.panelView = newView
        return newView
    }

    protected abstract fun onCreateView(container: ViewGroup): View

    protected open fun onOpen(info: BaseTechoItem) {}

    protected open fun onClose() {}

    protected open fun doAnimation(isOpen: Boolean) {
        // TODO
    }

    fun destroy() {
        // TODO
    }

    protected open fun onDestroy() {}

    fun callClose() {
        controller?.callClose(this)
    }

}