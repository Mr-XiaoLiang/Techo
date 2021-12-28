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

    private var currentInfo: BaseTechoItem? = null

    var isChangedValue = false
        private set

    fun setController(controller: PanelController?) {
        this.controller = controller
    }

    fun open(info: BaseTechoItem) {
        currentInfo = info
        isChangedValue = false
        panelView?.post {
            onOpen(info)
            doAnimation(true)
        }
    }

    fun close() {
        onClose()
        this.controller = null
        panelView?.post {
            doAnimation(false)
        }
    }

    fun getPanelView(container: ViewGroup): View {
        panelView?.let {
            return it
        }
        val newView = onCreateView(container)
        this.panelView = newView
        onViewCreated(newView)
        return newView
    }

    protected abstract fun onCreateView(container: ViewGroup): View

    protected open fun onViewCreated(view: View) {}

    protected open fun onOpen(info: BaseTechoItem) {}

    protected open fun onClose() {}

    protected fun notifyDataChanged() {
        isChangedValue = true
    }

    protected open fun doAnimation(isOpen: Boolean) {
        // TODO
    }

    fun destroy() {
        // TODO
    }

    protected fun getCurrentInfo(): BaseTechoItem? {
        return currentInfo
    }

    protected inline fun <reified T : BaseTechoItem> tryChangeInfo(callback: (T) -> Unit) {
        getCurrentInfo()?.let {
            if (it is T) {
                callback(it)
            }
        }
    }

    protected fun clickToClose(vararg views: View) {
        views.forEach {
            it.setOnClickListener {
                callClose()
            }
        }
    }

    protected fun keepWithClick(vararg views: View) {
        views.forEach {
            it.setOnClickListener {
                // 点击事件设置，但是不响应
            }
        }
    }

    protected open fun onDestroy() {}

    fun callClose() {
        controller?.callClose(this)
    }

}