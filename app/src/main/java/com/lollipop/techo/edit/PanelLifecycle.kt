package com.lollipop.techo.edit

/**
 * @author lollipop
 * @date 2021/12/18 20:23
 */
interface PanelLifecycle {

    fun onOpen(delegate: EditDelegate)

    fun onShow(delegate: EditDelegate)

    fun onClose(delegate: EditDelegate)

    fun onHide(delegate: EditDelegate)

}