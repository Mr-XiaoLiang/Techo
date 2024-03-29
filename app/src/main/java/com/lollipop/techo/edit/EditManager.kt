package com.lollipop.techo.edit

import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import androidx.appcompat.app.AppCompatActivity
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.edit.base.EditDelegate
import com.lollipop.techo.edit.impl.CheckBoxEditDelegate
import com.lollipop.techo.edit.impl.NumberEditDelegate
import com.lollipop.techo.edit.impl.PhotoEditDelegate
import com.lollipop.techo.edit.impl.SplitEditDelegate
import com.lollipop.techo.edit.impl.TextEditDelegate
import com.lollipop.techo.edit.impl.TitleEditDelegate
import com.lollipop.techo.util.permission.PermissionLauncher
import com.lollipop.techo.util.permission.PermissionLauncherManager

/**
 * @author lollipop
 * @date 2021/12/18 20:13
 */
class EditManager(
    private val activity: AppCompatActivity,
    private val container: ViewGroup
) : PanelController {

    companion object {
        private val PANEL_LIST: Array<Class<out EditDelegate<*>>> = arrayOf(
            PhotoEditDelegate::class.java,
            TextEditDelegate::class.java,
            CheckBoxEditDelegate::class.java,
            NumberEditDelegate::class.java,
            SplitEditDelegate::class.java,
            TitleEditDelegate::class.java
        )
    }

    private val editDelegateList = ArrayList<EditDelegate<*>>()
    private val panelViewMap = ArrayList<PanelView>()

    private var activeDelegate: EditDelegate<*>? = null
    private var editItemIndex = 0
    private var editInfo: TechoItem? = null
    private var onEditPanelCloseListener: OnEditPanelCloseListener? = null

    private val permissionLauncherManager = PermissionLauncherManager(activity)

    fun initPanel() {
        editDelegateList.clear()
        PANEL_LIST.forEach { panelClass ->
            val panel = panelClass.getDeclaredConstructor().newInstance()
            panel.permissions.forEach { info ->
                permissionLauncherManager.register(
                    who = panelClass,
                    permissions = info.permission,
                    rationale = info.rationale,
                    anyOne = info.anyOne
                )
            }
        }
        permissionLauncherManager.registerLauncher()
    }

    fun openEditPanel(index: Int = 0, info: TechoItem, listener: OnEditPanelCloseListener?) {
        closePanel()
        openEditByInfo(index, info, listener)
    }

    fun closePanel() {
        tryClosePanel(this.activeDelegate)
    }

    private fun openEditByInfo(
        index: Int = 0,
        info: TechoItem,
        listener: OnEditPanelCloseListener?
    ) {
        when (info) {
            is TechoItem.Photo -> {
                findDelegate<PhotoEditDelegate>().applyDelegate(index, info, listener).open(info)
            }

            is TechoItem.Text -> {
                findDelegate<TextEditDelegate>().applyDelegate(index, info, listener).open(info)
            }

            is TechoItem.CheckBox -> {
                findDelegate<CheckBoxEditDelegate>().applyDelegate(index, info, listener).open(info)
            }

            is TechoItem.Number -> {
                findDelegate<NumberEditDelegate>().applyDelegate(index, info, listener).open(info)
            }

            is TechoItem.Split -> {
                findDelegate<SplitEditDelegate>().applyDelegate(index, info, listener).open(info)
            }

            is TechoItem.Title -> {
                findDelegate<TitleEditDelegate>().applyDelegate(index, info, listener).open(info)
            }

            is TechoItem.Recording -> {
                // TODO()
            }

            is TechoItem.Vcr -> {
                // TODO()
            }
        }
    }

    private inline fun <reified T : EditDelegate<*>> T.applyDelegate(
        index: Int = 0,
        info: TechoItem,
        listener: OnEditPanelCloseListener?
    ): T {
        setController(this@EditManager)
        updateContextInfo(this, index, info, listener)
        checkPanelView(this)
        return this
    }

    private fun updateContextInfo(
        editDelegate: EditDelegate<*>,
        index: Int,
        info: TechoItem,
        listener: OnEditPanelCloseListener?
    ) {
        this.activeDelegate = editDelegate
        this.editItemIndex = index
        this.editInfo = info
        this.onEditPanelCloseListener = listener
    }

    private fun checkPanelView(panel: EditDelegate<*>) {
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

    private inline fun <reified T : EditDelegate<*>> findDelegate(): T {
        editDelegateList.forEach {
            if (it is T) {
                return it
            }
        }
        val newDelegate = T::class.java.getDeclaredConstructor().newInstance()
        editDelegateList.add(newDelegate)
        return newDelegate
    }

    private fun View?.tryRemoveFromParent() {
        val view = this
        view?.parent?.let { parent ->
            if (parent is ViewManager) {
                parent.removeView(view)
            }
        }
    }

    override fun callClose(editDelegate: EditDelegate<*>) {
        tryClosePanel(editDelegate)
    }

    override val context: AppCompatActivity
        get() = activity

    override fun findLauncher(who: Any, permission: Array<String>): PermissionLauncher? {
        return permissionLauncherManager.findLauncher(who::class.java, permission)
    }

    private fun tryClosePanel(editDelegate: EditDelegate<*>?) {
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
        fun onEditPanelClose(index: Int, info: TechoItem)
    }

    private class PanelView(
        val view: View,
        val panel: EditDelegate<*>
    )

}