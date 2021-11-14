package com.lollipop.base.list

import androidx.recyclerview.widget.*

/**
 * @author lollipop
 * @date 2021/11/14 22:49
 */
interface TouchableHolder {

    val canDrag: Boolean

    val canSwipe: Boolean

}

interface OnItemTouchCallback {
    /**
     * 当某个Item被滑动删除的时候
     *
     * @param adapterPosition item的position
     */
    fun onSwiped(adapterPosition: Int)

    /**
     * 当两个Item位置互换的时候被回调
     *
     * @param srcPosition    拖拽的item的position
     * @param targetPosition 目的地的Item的position
     * @return 开发者处理了操作应该返回true，开发者没有处理就返回false
     */
    fun onMove(srcPosition: Int, targetPosition: Int): Boolean
}

interface OnItemTouchStateChangedListener {
    fun onItemTouchStateChanged(viewHolder: RecyclerView.ViewHolder?, status: ItemTouchState)
}

enum class ItemTouchState {
    IDLE, SWIPE, DRAG
}

private class LItemTouchCallback(
    private val callback: OnItemTouchCallback?,
    private val statusCallback: OnItemTouchStateChangedListener?
) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val layoutManager = recyclerView.layoutManager
        val flags = when (layoutManager) {
            is GridLayoutManager -> {// GridLayoutManager
                // flag如果值是0，相当于这个功能被关闭
                val dragFlag =
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlag = if (layoutManager.orientation == GridLayoutManager.VERTICAL) {
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                } else {
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN
                }
                intArrayOf(dragFlag, swipeFlag)
            }

            is LinearLayoutManager -> {
                // linearLayoutManager
                val orientation = layoutManager.orientation

                val dragFlag = if (orientation == LinearLayoutManager.HORIZONTAL) {
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                } else {
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN
                }


                val swipeFlag = if (orientation == LinearLayoutManager.HORIZONTAL) {
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN
                } else {
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                }
                intArrayOf(dragFlag, swipeFlag)
            }

            is StaggeredGridLayoutManager -> {
                // flag如果值是0，相当于这个功能被关闭
                val dragFlag =
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN
                val swipeFlag =
                    if (layoutManager.orientation == StaggeredGridLayoutManager.VERTICAL) {
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    } else {
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    }
                intArrayOf(dragFlag, swipeFlag)
            }

            else -> intArrayOf(0, 0)
        }.apply {
            if (viewHolder is TouchableHolder) {
                if (!viewHolder.canDrag) {
                    this[0] = 0
                }
                if (!viewHolder.canSwipe) {
                    this[1] = 0
                }
            }
        }
        return makeMovementFlags(flags[0], flags[1])
    }

    /**
     * 是否可以拖拽
     */
    var isCanDrag = false

    /**
     * 是否可以被滑动
     */
    var isCanSwipe = false

    override fun isLongPressDragEnabled(): Boolean {
        return isCanDrag && callback != null
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return isCanSwipe && callback != null
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return callback?.onMove(viewHolder.adapterPosition, target.adapterPosition) ?: false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        callback?.onSwiped(viewHolder.adapterPosition)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        statusCallback?.onItemTouchStateChanged(
            viewHolder,
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_SWIPE -> {
                    ItemTouchState.SWIPE
                }
                ItemTouchHelper.ACTION_STATE_DRAG -> {
                    ItemTouchState.DRAG
                }
                ItemTouchHelper.ACTION_STATE_IDLE -> {
                    ItemTouchState.IDLE
                }
                else -> {
                    ItemTouchState.IDLE
                }
            }
        )
    }

}

class LItemTouchHelperBuilder(private val recyclerView: RecyclerView) {

    private var touchCallback: OnItemTouchCallback? = null
    private var statusCallback: OnItemTouchStateChangedListener? = null
    private var isCanDrag = false
    private var isCanSwipe = false

    // TODO

}