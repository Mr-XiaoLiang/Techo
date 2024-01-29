package com.lollipop.techo.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lollipop.base.list.LoadMoreHelper
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.base.util.registerResult
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.tint
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoMode
import com.lollipop.techo.data.TechoMode.ChangedType.Delete
import com.lollipop.techo.data.TechoMode.ChangedType.Full
import com.lollipop.techo.data.TechoMode.ChangedType.Insert
import com.lollipop.techo.data.TechoMode.ChangedType.Modify
import com.lollipop.techo.data.TechoMode.ChangedType.Move
import com.lollipop.techo.databinding.ActivityMainBinding
import com.lollipop.techo.databinding.ActivityMainFloatingBinding
import com.lollipop.techo.list.home.HomeListAdapter

class MainActivity : HeaderActivity(),
    SwipeRefreshLayout.OnRefreshListener,
    TechoMode.StateListener {

    private val viewBinding: ActivityMainBinding by lazyBind()
    private val floatingBinding: ActivityMainFloatingBinding by lazyBind()

    override val showBackArrow = false

    override val contentView: View
        get() = viewBinding.root

    override val floatingView: View
        get() = floatingBinding.root

    override val optionsMenu: Int
        get() = R.menu.menu_main

    private val mode by lazy {
        TechoMode.create(this).attach(this).buildListMode()
    }

    private val loadMoreHelper by lazy {
        LoadMoreHelper.bind(viewBinding.techoListView, ::onLoadMore)
    }

    private val editPageLauncher = registerResult(TechoEditActivity.LAUNCHER) { newId ->
        if (newId != TechoEditActivity.NO_ID) {
            mode.onNewInsert(newId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        viewBinding.swipeRefreshLayout.setOnRefreshListener(this)
        viewBinding.techoListView.layoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        viewBinding.techoListView.adapter = HomeListAdapter(mode.info)

        // 调用触发实例化
        loadMoreHelper

        floatingBinding.newTechoBtn.onClick {
            editPageLauncher.launch(null)
//            requestActivity<RecorderActivity> {
//                if (it.isOk) {
//                    Toast.makeText(
//                        this,
//                        RecorderActivity.getAudioFile(it.data),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
        }

        floatingBinding.root.fixInsetsByPadding(WindowInsetsEdge.ALL)
        viewBinding.root.fixInsetsByPadding(WindowInsetsEdge.CONTENT)
    }

    override fun onLoadStart() {
        if (viewBinding.swipeRefreshLayout.isRefreshing) {
            return
        }
        showLoading()
    }

    override fun onLoadEnd() {
        viewBinding.swipeRefreshLayout.isRefreshing = false
        hideLoading()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onInfoChanged(start: Int, count: Int, type: TechoMode.ChangedType) {
        val adapter = viewBinding.techoListView.adapter ?: return
        when (type) {
            Full -> {
                adapter.notifyDataSetChanged()
            }

            Modify -> {
                adapter.notifyItemRangeChanged(start, count)
            }

            Insert -> {
                adapter.notifyItemRangeInserted(start, count)
            }

            Delete -> {
                adapter.notifyItemRangeRemoved(start, count)
            }

            Move -> {
                adapter.notifyItemMoved(start, count)
            }
        }
        loadMoreHelper.isLoading = false
    }

    override fun onRefresh() {
        mode.refresh()
    }

    private fun onLoadMore() {
        mode.loadNext()
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        floatingBinding.newTechoBtn.tint(pigment)
    }

}