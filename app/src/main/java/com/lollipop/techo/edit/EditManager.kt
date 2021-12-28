package com.lollipop.techo.edit

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import com.lollipop.techo.data.BaseTechoItem
import com.lollipop.techo.edit.impl.PhotoEditDelegate
import com.lollipop.techo.edit.impl.SplitEditDelegate
import com.lollipop.techo.edit.impl.TextEditDelegate

/**
 * @author lollipop
 * @date 2021/12/18 20:13
 */
class EditManager(
    private val activity: Activity,
    private val container: ViewGroup
) : PanelController {

    companion object {
        private val EDIT_DELEGATE_POOL: Array<Class<out EditDelegate>> = arrayOf(
            TextEditDelegate::class.java,
            PhotoEditDelegate::class.java,
            SplitEditDelegate::class.java,
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
        val findPanel = panelViewMap.filter { it.panel == panel }.toMutableList()
        val view = panel.getPanelView(container)
        if (findPanel.isNotEmpty()) {
            val removedList = ArrayList<PanelView>(findPanel.size)
            var targetPanel: PanelView? = null
            findPanel.forEach {
                when {
                    it.view != view -> {
                        removedList.add(it)
                    }
                    it.view.parent != container -> {
                        it.view.tryRemoveFromParent()
                    }
                    it.view.parent == container -> {
                        if (targetPanel == null) {
                            targetPanel = it
                        }
                    }
                }
            }
            if (targetPanel != null) {
                findPanel.forEach {
                    if (it != targetPanel) {
                        panelViewMap.remove(it)
                        it.view.tryRemoveFromParent()
                    }
                }
                return
            }
            removedList.forEach {
                findPanel.remove(it)
                panelViewMap.remove(it)
                it.view.tryRemoveFromParent()
            }
            if (findPanel.isNotEmpty()) {
                val firstView = findPanel[0]
                findPanel.removeAt(0)
                if (findPanel.size > 1) {
                    findPanel.forEach {
                        panelViewMap.remove(it)
                        it.view.tryRemoveFromParent()
                    }
                }
                firstView.view.tryRemoveFromParent()
                container.addView(
                    firstView.view,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                return
            }
        }
        panelViewMap.add(PanelView(view, panel))
        container.addView(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun View?.tryRemoveFromParent() {
        val view = this
        view?.parent?.let { parent ->
            if (parent is ViewManager) {
                parent.removeView(view)
            }
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
            if (editDelegate?.isChangedValue == true) {
                this.editInfo?.let {
                    this.onEditPanelCloseListener?.onEditPanelClose(editItemIndex, it)
                }
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