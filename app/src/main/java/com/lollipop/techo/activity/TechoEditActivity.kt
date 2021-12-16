package com.lollipop.techo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lollipop.base.list.*
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.data.*
import com.lollipop.techo.data.TechoItemType.*
import com.lollipop.techo.databinding.ActivityTechoEditBinding
import com.lollipop.techo.databinding.ActivityTechoEditFloatingBinding
import com.lollipop.techo.list.DetailListAdapter
import com.lollipop.techo.util.CircleAnimationGroup

/**
 * 编辑 & 添加页
 */
class TechoEditActivity : HeaderActivity() {

    companion object {

        private const val PARAMETER_TECHO_ID = "PARAMETER_TECHO_ID"

        fun start(context: Context, infoId: Int = 0) {
            context.startActivity(
                    Intent(context, TechoEditActivity::class.java).apply {
                        putExtra(PARAMETER_TECHO_ID, infoId)
                    }
            )
        }
    }

    private val viewBinding: ActivityTechoEditBinding by lazyBind()

    private val floatingBinding: ActivityTechoEditFloatingBinding by lazyBind()

    private val dataList: MutableList<BaseTechoItem> = ArrayList()

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
            val listAdapter = DetailListAdapter(dataList).apply {
                changeEditMode(true)
            }
            adapter = listAdapter
            attachTouchHelper()
                    .canDrag(true)
                    .canSwipe(true)
                    .onMoveWithList(dataList) { srcPosition, targetPosition ->
                        listAdapter.notifyItemMoved(srcPosition, targetPosition)
                    }
                    .onSwipeWithList(dataList) {
                        listAdapter.notifyItemRemoved(it)
                    }
                    .onStatusChange(::onItemTouchStateChanged)
                    .apply()
        }
    }

    private fun initData() {
        // TODO
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
        floatingBinding.quickAddButton.setOnClickListener {
            addItemByType()
        }
        floatingBinding.floatingMenuBtn.setOnClickListener {
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

    private fun onItemTouchStateChanged(viewHolder: RecyclerView.ViewHolder?, status: ItemTouchState) {
        // TODO("Not yet implemented")
    }

    private fun addItemByType() {
        val newItem = when (quickAddType) {
            Empty -> {
                return
            }
            Text -> {
                TextItem()
            }
            Number -> {
                NumberItem()
            }
            CheckBox -> {
                CheckBoxItem()
            }
            Photo -> {
                PhotoItem()
            }
            Split -> {
                SplitItem()
            }
        }
        val size = dataList.size
        dataList.add(newItem)
        viewBinding.contentListView.adapter?.notifyItemInserted(size)
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