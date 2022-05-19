package com.lollipop.techo.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lollipop.base.list.ItemTouchState
import com.lollipop.base.list.attachTouchHelper
import com.lollipop.base.util.*
import com.lollipop.techo.data.TechoItemType.*
import com.lollipop.techo.data.TechoItemType.Number
import com.lollipop.techo.data.TechoMode
import com.lollipop.techo.data.TechoMode.ChangedType.*
import com.lollipop.techo.databinding.ActivityTechoEditBinding
import com.lollipop.techo.databinding.ActivityTechoEditFloatingBinding
import com.lollipop.techo.edit.EditManager
import com.lollipop.techo.list.detail.DetailListAdapter
import com.lollipop.techo.util.CircleAnimationGroup

/**
 * 编辑 & 添加页
 */
class TechoEditActivity : HeaderActivity(), TechoMode.StateListener {

    companion object {

        private const val PARAMETER_TECHO_ID = "PARAMETER_TECHO_ID"

        private const val RESULT_TECHO_ID = "RESULT_TECHO_ID"

        const val NO_ID = 0

        fun putParams(intent: Intent, infoId: Int = 0): Intent {
            return intent.putExtra(PARAMETER_TECHO_ID, infoId)
        }

        fun getResultTechoId(intent: Intent?): Int {
            intent ?: return NO_ID
            return intent.getIntExtra(RESULT_TECHO_ID, NO_ID)
        }

    }

    private val mode by lazy {
        TechoMode.create(this).attach(this).buildDetailMode()
    }

    private val viewBinding: ActivityTechoEditBinding by lazyBind()

    private val floatingBinding: ActivityTechoEditFloatingBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root

    override val floatingView: View
        get() = floatingBinding.root

    private val circleAnimationGroup by lazy {
        CircleAnimationGroup<FloatingActionButton>()
    }

    private val techoId by lazy {
        intent.getIntExtra(PARAMETER_TECHO_ID, 0)
    }

    private var quickAddType = Text

    private val editManager by lazy {
        EditManager(this, this, floatingBinding.editContainer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        floatingView.fixInsetsByPadding(WindowInsetsHelper.Edge.ALL)
        initContentView()
        initMenuBtn()
        initData()
    }

    private fun initContentView() {
        viewBinding.contentListView.apply {
            layoutManager = LinearLayoutManager(
                this@TechoEditActivity, RecyclerView.VERTICAL, false
            )
            val listAdapter = DetailListAdapter(mode.info.items).apply {
                changeEditMode(true)
            }
            adapter = listAdapter
            attachTouchHelper()
                .canDrag(true)
                .canSwipe(true)
                .onMove(mode)
                .onSwipe(mode)
                .onStatusChange(::onItemTouchStateChanged)
                .apply()
        }
    }

    private fun initData() {
        onUI {
            mode.loadOrCreate(techoId)
        }
    }

    private fun initMenuBtn() {
        circleAnimationGroup.setCenterView(floatingBinding.floatingMenuBtn)
        circleAnimationGroup.addPlanet(
            floatingBinding.floatingTextBtn to true,
            floatingBinding.floatingNumberBtn to true,
            floatingBinding.floatingCheckboxBtn to true,
            floatingBinding.floatingPhotoBtn to true,
            floatingBinding.floatingSplitBtn to true,
            floatingBinding.floatingTest6 to false,
            floatingBinding.floatingTest7 to false,
            floatingBinding.floatingTest8 to false,
            floatingBinding.floatingTest9 to false,
            floatingBinding.floatingTest10 to false,
            floatingBinding.floatingTest11 to false,
            floatingBinding.floatingTest12 to false,
            floatingBinding.floatingTest13 to false,
            floatingBinding.floatingTest14 to false,
        )
        circleAnimationGroup.onPlanetClick(::onFloatBtnClick)
        circleAnimationGroup.bindListener {
            onProgressUpdate { progress ->
                floatingBinding.floatingMenuBtn.rotation = progress * 135
            }
            onHideCalled {
                floatingBinding.quickAddButton.show()
            }
            onShowCalled {
                floatingBinding.quickAddButton.hide()
            }
        }
        floatingBinding.quickAddButton.onClick {
            addItemByType()
        }
        floatingBinding.floatingMenuBtn.onClick {
            if (circleAnimationGroup.isOpened) {
                circleAnimationGroup.close()
            } else {
                circleAnimationGroup.open()
            }
        }

        // 初始化默认的类型
        quickAddType = Text
        floatingBinding.quickAddButton.setImageDrawable(floatingBinding.floatingTextBtn.drawable)
        circleAnimationGroup.hide()
    }

    private fun onFloatBtnClick(btn: FloatingActionButton) {
        floatingBinding.quickAddButton.setImageDrawable(btn.drawable)
        when (btn) {
            floatingBinding.floatingTextBtn -> {
                quickAddType = Text
            }
            floatingBinding.floatingNumberBtn -> {
                quickAddType = Number
            }
            floatingBinding.floatingCheckboxBtn -> {
                quickAddType = CheckBox
            }
            floatingBinding.floatingPhotoBtn -> {
                quickAddType = Photo
            }
            floatingBinding.floatingSplitBtn -> {
                quickAddType = Split
            }
        }
        circleAnimationGroup.close()
        addItemByType()
    }

    private fun onItemTouchStateChanged(
        viewHolder: RecyclerView.ViewHolder?,
        status: ItemTouchState
    ) {
        if (status == ItemTouchState.IDLE) {
            mode.format()
        }
    }

    private fun addItemByType() {
        mode.insert(quickAddType)
    }

    override fun onLoadStart() {
        showLoading()
    }

    override fun onLoadEnd() {
        hideLoading()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onInfoChanged(start: Int, count: Int, type: TechoMode.ChangedType) {
        viewBinding.contentListView.apply {
            when (type) {
                Full -> {
                    adapter?.notifyDataSetChanged()
                }
                Modify -> {
                    adapter?.notifyItemRangeChanged(start, count)
                }
                Insert -> {
                    adapter?.notifyItemRangeInserted(start, count)
                }
                Delete -> {
                    adapter?.notifyItemRangeRemoved(start, count)
                }
                Move -> {
                    adapter?.notifyItemMoved(start, count)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (circleAnimationGroup.isOpened) {
            circleAnimationGroup.close()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        circleAnimationGroup.destroy()
    }

}