package com.lollipop.base.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager

open class ItemDecorationHelper : RecyclerView.ItemDecoration() {

    companion object {
        fun clearItemDecoration(recyclerView: RecyclerView) {
            while (recyclerView.itemDecorationCount > 0) {
                recyclerView.removeItemDecorationAt(0)
            }
        }
    }

    protected open val startSpace = 0
    protected open val topSpace = 0
    protected open val endSpace = 0
    protected open val bottomSpace = 0
    protected open val verticalInterval = 0
    protected open val horizontalInterval = 0

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val itemCount = parent.adapter?.itemCount ?: return
        if (itemCount == 0) {
            return
        }
        val layoutManager = parent.layoutManager ?: return
        val rtl = isRtl(layoutManager)
        val position = layoutManager.getPosition(view)
        when (layoutManager) {
            is GridLayoutManager -> {
                getItemOffsetsByGrid(
                    outRect = outRect,
                    view = view,
                    parent = parent,
                    state = state,
                    layoutManager = layoutManager,
                    position = position,
                    itemCount = itemCount,
                    rtl = rtl
                )
            }
            is StaggeredGridLayoutManager -> {
                getItemOffsetsByStaggeredGrid(
                    outRect = outRect,
                    view = view,
                    parent = parent,
                    state = state,
                    layoutManager = layoutManager,
                    position = position,
                    itemCount = itemCount,
                    rtl = rtl
                )
            }
            is LinearLayoutManager -> {
                getItemOffsetsByLinear(
                    outRect = outRect,
                    view = view,
                    parent = parent,
                    state = state,
                    layoutManager = layoutManager,
                    position = position,
                    itemCount = itemCount,
                    rtl = rtl
                )
            }
            else -> {
                throw java.lang.RuntimeException("这是一个不受支持的LayoutManager")
            }
        }
    }

    protected open fun getItemOffsetsByGrid(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
        layoutManager: GridLayoutManager,
        position: Int,
        itemCount: Int,
        rtl: Boolean
    ) {
        val orientation = layoutManager.orientation
        val spanCount = layoutManager.spanCount
        if (orientation == RecyclerView.HORIZONTAL) {
            // 水平排列
            val isTop = getRow(position, spanCount) == 0
            val isBottom = isRowEnd(position, spanCount)
            val isStart = getLineNumber(position, spanCount) == 0
            val isEnd = isLastLine(position, spanCount, itemCount)
            setOutRect(outRect, isStart, isTop, isEnd, isBottom, rtl)
        } else {
            // 垂直排列
            val isTop = getLineNumber(position, spanCount) == 0
            val isBottom = isLastLine(position, spanCount, itemCount)
            val isStart = getRow(position, spanCount) == 0
            val isEnd = isRowEnd(position, spanCount)
            setOutRect(outRect, isStart, isTop, isEnd, isBottom, rtl)
        }
    }

    protected open fun getItemOffsetsByLinear(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
        layoutManager: LinearLayoutManager,
        position: Int,
        itemCount: Int,
        rtl: Boolean
    ) {
        val orientation = layoutManager.orientation
        if (orientation == RecyclerView.HORIZONTAL) {
            // 水平排列
            val isTop = true
            val isBottom = true
            val isStart = position == 0
            val isEnd = position == (itemCount - 1)
            setOutRect(outRect, isStart, isTop, isEnd, isBottom, rtl)
        } else {
            // 垂直排列
            val isTop = position == 0
            val isBottom = position == (itemCount - 1)
            val isStart = true
            val isEnd = true
            setOutRect(outRect, isStart, isTop, isEnd, isBottom, rtl)
        }
    }

    protected open fun getItemOffsetsByStaggeredGrid(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
        layoutManager: StaggeredGridLayoutManager,
        position: Int,
        itemCount: Int,
        rtl: Boolean
    ) {
        val orientation = layoutManager.orientation
        val spanCount = layoutManager.spanCount
        val layoutParams = view.layoutParams
        if (layoutParams !is StaggeredGridLayoutManager.LayoutParams) {
            return
        }
        val spanIndex = layoutParams.spanIndex
        val fullSpan = layoutParams.isFullSpan
        if (orientation == RecyclerView.HORIZONTAL) {
            // 水平排列
            val isTop = fullSpan || spanIndex == 0
            val isBottom = fullSpan || spanIndex == spanCount - 1
            val isStart = false
            val isEnd = false
            setOutRect(outRect, isStart, isTop, isEnd, isBottom, rtl)
        } else {
            // 垂直排列
            val isTop = false
            val isBottom = false
            val isStart = fullSpan || spanIndex == 0
            val isEnd = fullSpan || spanIndex == spanCount - 1
            setOutRect(outRect, isStart, isTop, isEnd, isBottom, rtl)
        }
    }

    protected fun isRtl(layoutManager: LayoutManager): Boolean {
        return layoutManager.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }

    protected fun leftSpace(isRtl: Boolean): Int {
        return if (isRtl) {
            endSpace
        } else {
            startSpace
        }
    }

    protected fun rightSpace(isRtl: Boolean): Int {
        return if (isRtl) {
            startSpace
        } else {
            endSpace
        }
    }

    protected fun getLineNumber(position: Int, spanCount: Int): Int {
        var line = position / spanCount
        if (position / spanCount != 0) {
            line++
        }
        return line
    }

    protected fun isLastLine(position: Int, spanCount: Int, allCount: Int): Boolean {
        return getLineNumber(position, spanCount) == getLineNumber(allCount - 1, spanCount)
    }

    protected fun getRow(position: Int, spanCount: Int): Int {
        return position % spanCount
    }

    protected fun isRowEnd(position: Int, spanCount: Int): Boolean {
        return getRow(position, spanCount) == (spanCount - 1)
    }

    protected fun setOutRect(
        outRect: Rect,
        isStart: Boolean,
        isTop: Boolean,
        isEnd: Boolean,
        isBottom: Boolean,
        isRtl: Boolean
    ) {
        outRect.top = if (isTop) {
            topSpace
        } else {
            verticalInterval / 2
        }
        outRect.bottom = if (isBottom) {
            bottomSpace
        } else {
            verticalInterval / 2
        }
        val start = if (isStart) {
            startSpace
        } else {
            horizontalInterval / 2
        }
        val end = if (isEnd) {
            endSpace
        } else {
            horizontalInterval / 2
        }
        if (isRtl) {
            outRect.left = end
            outRect.right = start
        } else {
            outRect.left = start
            outRect.right = end
        }
    }

}