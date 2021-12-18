package com.lollipop.techo.edit

import com.lollipop.techo.data.BaseTechoItem

/**
 * @author lollipop
 * @date 2021/12/18 20:15
 */
abstract class EditDelegate {

    abstract fun isSupport(info: BaseTechoItem): Boolean

    private var manager: EditManager? = null

    fun setManager(manager: EditManager?) {
        this.manager = manager
    }

    fun open(info: BaseTechoItem) {
        doAnimation(true)
        onOpen(info)
    }

    fun close() {
        doAnimation(false)
        onClose()
    }

    open fun onOpen(info: BaseTechoItem) {}

    open fun onClose() {}

    open fun doAnimation(isOpen: Boolean) {
        // TODO
    }

    fun callClose() {
        manager?.closePanel()
    }

}