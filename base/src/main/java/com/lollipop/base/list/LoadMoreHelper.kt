package com.lollipop.base.list

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class LoadMoreHelper(
    private val callback: OnLoadMoreCallback
) : RecyclerView.OnScrollListener() {

    companion object {
        fun bind(recyclerView: RecyclerView, callback: OnLoadMoreCallback): LoadMoreHelper {
            val loadMoreHelper = LoadMoreHelper(callback)
            recyclerView.addOnScrollListener(loadMoreHelper)
            return loadMoreHelper
        }
    }

    var isLoading = false

    var isEnable = true

    var buffSize = 5

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (!isEnable || isLoading) {
            return
        }
        val layoutManager = recyclerView.layoutManager ?: return
        recyclerView.adapter ?: return
        Log.d("LoadMoreHelper", "dy: $dy")
        when (layoutManager) {
            is LinearLayoutManager -> {
                onScrolledByLinearLayoutManager(recyclerView, layoutManager)
            }
            is StaggeredGridLayoutManager -> {
                onScrolledByStaggeredGridLayoutManager(recyclerView, layoutManager)
            }
            else -> {
                throw RuntimeException("不支持的LayoutManager")
            }
        }
    }

    private fun onScrolledByLinearLayoutManager(
        recyclerView: RecyclerView,
        layoutManager: LinearLayoutManager
    ) {
        val adapter = recyclerView.adapter ?: return
        val itemCount = adapter.itemCount
        if (itemCount < buffSize) {
            callLoadMore()
            return
        }
        val lastPosition = layoutManager.findLastVisibleItemPosition()
        if (lastPosition + 5 >= itemCount) {
            callLoadMore()
        }
    }

    private fun onScrolledByStaggeredGridLayoutManager(
        recyclerView: RecyclerView,
        layoutManager: StaggeredGridLayoutManager
    ) {
        val adapter = recyclerView.adapter ?: return
        val itemCount = adapter.itemCount
        if (itemCount < buffSize) {
            callLoadMore()
            return
        }
        val lastPositionArray =
            layoutManager.findLastVisibleItemPositions(IntArray(layoutManager.spanCount))
        var lastPosition = 0
        lastPositionArray.forEach {
            if (it > lastPosition) {
                lastPosition = it
            }
        }
        if (lastPosition + 5 >= itemCount) {
            callLoadMore()
        }
    }

    private fun callLoadMore() {
        isLoading = true
        callback.onLoadMore()
    }

    fun interface OnLoadMoreCallback {
        fun onLoadMore()
    }
}