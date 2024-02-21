package com.lollipop.techo.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lollipop.base.list.LoadMoreHelper
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityBasicListBinding

open class BasicListActivity : HeaderActivity(), SwipeRefreshLayout.OnRefreshListener {

    private val contentBinding: ActivityBasicListBinding by lazyBind()

    override val contentView: View
        get() {
            return contentBinding.root
        }

    private val loadMoreHelper by lazy {
        LoadMoreHelper.bind(recyclerView, ::onLoadMore)
    }

    protected val recyclerView: RecyclerView
        get() {
            return contentBinding.recyclerView
        }

    protected val refreshLayout: SwipeRefreshLayout
        get() {
            return contentBinding.refreshLayout
        }

    protected var isMoreLoading: Boolean
        get() {
            return loadMoreHelper.isLoading
        }
        set(value) {
            loadMoreHelper.isLoading = value
        }

    protected var isRefreshing: Boolean
        get() {
            return refreshLayout.isRefreshing
        }
        set(value) {
            refreshLayout.isRefreshing = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentView.fixInsetsByPadding(WindowInsetsEdge.CONTENT)
        refreshLayout.setOnRefreshListener(this)
        isEnableLoadMore(false)
        isEnableRefresh(false)
    }

    protected fun initRecyclerView(callback: (RecyclerView) -> Unit) {
        callback(recyclerView)
    }

    protected fun isEnableLoadMore(enable: Boolean) {
        loadMoreHelper.isEnable = enable
    }

    protected fun isEnableRefresh(enable: Boolean) {
        refreshLayout.isEnabled = enable
    }

    open fun onLoadMore() {

    }

    override fun onRefresh() {
    }

}