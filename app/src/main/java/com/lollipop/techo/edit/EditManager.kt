package com.lollipop.techo.edit

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.lollipop.techo.data.BaseTechoItem

/**
 * @author lollipop
 * @date 2021/12/18 20:13
 */
class EditManager(
    private val activity: Activity,
    private val container: ViewGroup
) : PanelController {

    companion object {
        private val EDIT_DELEGATE_POOL: Array<Class<EditDelegate>> = arrayOf(

        )
    }

    private val editDelegateList = ArrayList<EditDelegate>()
    private val panelViewMap = ArrayList<PanelView>()

    private var activeDelegate: EditDelegate? = null
    private var editItemIndex = 0
    private var editInfo: BaseTechoItem? = null
    private var onEditPanelCloseListener: OnEditPanelCloseListener? = null

    init {
        editDelegateList.clear()
        EDIT_DELEGATE_POOL.forEach {
            val element = it.newInstance()
            element.setController(this)
            editDelegateList.add(element)
        }
    }

    fun openPanel(index: Int = 0, info: BaseTechoItem, listener: OnEditPanelCloseListener?) {
        val supported = editDelegateList.filter { it.isSupport(info) }
        if (supported.isEmpty()) {
            return
        }
        closePanel()
        val editDelegate = supported[0]
        this.activeDelegate = editDelegate
        this.editItemIndex = index
        this.editInfo = info
        this.onEditPanelCloseListener = listener
        checkPanelView(editDelegate)
        editDelegate.open(info)
    }

    fun closePanel() {
        tryClosePanel(this.activeDelegate)
    }

    private fun checkPanelView(panel: EditDelegate) {
        val findPanel = panelViewMap.filter { it.panel == panel }
        if (findPanel.isEmpty()) {
            val view = panel.getPanelView(container)
            panelViewMap.add(PanelView(view, panel))
            container.addView(
                view,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun callClose(editDelegate: EditDelegate) {
        tryClosePanel(editDelegate)
    }

    override val context: Activity
        get() = activity

    private fun tryClosePanel(editDelegate: EditDelegate?) {
        editDelegate?.close()
        if (editDelegate == this.activeDelegate) {
            this.editInfo?.let {
                this.onEditPanelCloseListener?.onEditPanelClose(editItemIndex, it)
            }
            this.editInfo = null
            this.onEditPanelCloseListener = null
            this.editItemIndex = 0
            this.activeDelegate = null
        }
    }

    fun destroy() {
        this.editInfo = null
        this.onEditPanelCloseListener = null
        this.editItemIndex = 0
        this.activeDelegate = null
        this.editDelegateList.forEach {
            it.destroy()
            it.setController(null)
        }
        this.editDelegateList.clear()
        panelViewMap.clear()
    }

    fun interface OnEditPanelCloseListener {
        fun onEditPanelClose(index: Int, info: BaseTechoItem)
    }

    private class PanelView(
        val view: View,
        val panel: EditDelegate
    )

}