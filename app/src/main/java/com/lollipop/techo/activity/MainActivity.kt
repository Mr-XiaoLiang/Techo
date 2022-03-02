package com.lollipop.techo.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.techo.data.TechoMode
import com.lollipop.techo.data.TechoMode.ChangedType.*
import com.lollipop.techo.databinding.ActivityMainBinding
import com.lollipop.techo.databinding.ActivityMainFloatingBinding
import com.lollipop.techo.list.home.HomeListAdapter

class MainActivity : HeaderActivity(),
    SwipeRefreshLayout.OnRefreshListener,
    TechoMode.StateListener {

    private val viewBinding: ActivityMainBinding by lazyBind()
    private val floatingBinding: ActivityMainFloatingBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root

    override val floatingView: View
        get() = floatingBinding.root

    private val mode by lazy {
        TechoMode.create(this).attach(this).buildListMode()
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

        floatingBinding.newTechoBtn.onClick {
            requestActivity<TechoEditActivity> {
                if (it.isOk) {
                    val newId = TechoEditActivity.getResultTechoId(it.data)
                    if (newId != TechoEditActivity.NO_ID) {
                        mode.onNewInsert(newId)
                    }
                }
            }
        }
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
        when (type) {
            Full -> {
                viewBinding.techoListView.adapter?.notifyDataSetChanged()
            }
            Modify -> {
                viewBinding.techoListView.adapter?.notifyItemRangeChanged(start, count)
            }
            Insert -> {
                viewBinding.techoListView.adapter?.notifyItemRangeInserted(start, count)
            }
            Delete -> {
                viewBinding.techoListView.adapter?.notifyItemRangeRemoved(start, count)
            }
            Move -> {
                viewBinding.techoListView.adapter?.notifyItemMoved(start, count)
            }
        }
    }

    override fun onRefresh() {
        mode.refresh()
    }

}