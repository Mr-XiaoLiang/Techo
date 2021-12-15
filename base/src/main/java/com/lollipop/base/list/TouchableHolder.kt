package com.lollipop.base.list

import androidx.recyclerview.widget.*
import java.util.*

/**
 * @author lollipop
 * @date 2021/11/14 22:49
 */

/**
 * 手势的Holder接口
 */
interface TouchableHolder {

    val canDrag: Boolean

    val canSwipe: Boolean

}

/**
 * 滑动移除的回调函数
 */
fun interface OnItemSwipeCallback {
    /**
     * 当某个Item被滑动删除的时候
     *
     * @param adapterPosition item的position
     */
    fun onSwipe(adapterPosition: Int)
}

/**
 * 拖拽排序的回调函数
 */
fun interface OnItemMoveCallback {
    /**
     * 当两个Item位置互换的时候被回调
     *
     * @param srcPosition    拖拽的item的position
     * @param targetPosition 目的地的Item的position
     * @return 开发者处理了操作应该返回true，开发者没有处理就返回false
     */
    fun onMove(srcPosition: Int, targetPosition: Int): Boolean
}

/**
 * 滑动状态的切换监听
 */
fun interface OnItemTouchStateChangedListener {
    fun onItemTouchStateChanged(viewHolder: RecyclerView.ViewHolder?, status: ItemTouchState)
}

/**
 * 状态提供者
 */
fun interface StatusProvider {
    fun getStatus(): Boolean
}

/**
 * 滑动状态的枚举
 */
enum class ItemTouchState {
    /**
     * 滑动停止，闲置状态
     */
    IDLE,

    /**
     * 正在滑动移除
     */
    SWIPE,

    /**
     * 正在做交换
     */
    DRAG
}

private class LItemTouchCallback(
        private val dragStatusProvider: StatusProvider,
        private val swipeStatusProvider: StatusProvider,
        private val swipeCallback: OnItemSwipeCallback?,
        private val moveCallback: OnItemMoveCallback?,
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

    override fun isLongPressDragEnabled(): Boolean {
        return dragStatusProvider.getStatus() && moveCallback != null
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return swipeStatusProvider.getStatus() && swipeCallback != null
    }

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean {
        return moveCallback?.onMove(viewHolder.adapterPosition, target.adapterPosition) ?: false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipeCallback?.onSwipe(viewHolder.adapterPosition)
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

    private var swipeCallback: OnItemSwipeCallback? = null
    private var moveCallback: OnItemMoveCallback? = null
    private var statusCallback: OnItemTouchStateChangedListener? = null
    private var dragStatusProvider: StatusProvider? = null
    private var swipeStatusProvider: StatusProvider? = null

    fun onSwipe(callback: OnItemSwipeCallback): LItemTouchHelperBuilder {
        this.swipeCallback = callback
        return this
    }

    fun onSwipeWithList(
            list: MutableList<out Any>,
            onSwiped: (adapterPosition: Int) -> Unit
    ): LItemTouchHelperBuilder {
        return onSwipe(SimpleSwipeImpl(list, onSwiped))
    }

    fun onMove(callback: OnItemMoveCallback): LItemTouchHelperBuilder {
        this.moveCallback = callback
        return this
    }

    fun onMoveWithList(
            list: List<Any>,
            onMoved: (srcPosition: Int, targetPosition: Int) -> Unit
    ): LItemTouchHelperBuilder {
        return onMove(SimpleMoveImpl(list, onMoved))
    }

    fun onStatusChange(callback: OnItemTouchStateChangedListener): LItemTouchHelperBuilder {
        this.statusCallback = callback
        return this
    }

    fun canDrag(callback: StatusProvider): LItemTouchHelperBuilder {
        dragStatusProvider = callback
        return this
    }

    fun canSwipe(callback: StatusProvider): LItemTouchHelperBuilder {
        swipeStatusProvider = callback
        return this
    }

    fun canDrag(status: Boolean): LItemTouchHelperBuilder {
        return canDrag { status }
    }

    fun canSwipe(status: Boolean): LItemTouchHelperBuilder {
        return canSwipe { status }
    }

    fun apply() {
        ItemTouchHelper(
                LItemTouchCallback(
                        dragStatusProvider = dragStatusProvider ?: StatusProvider { false },
                        swipeStatusProvider = swipeStatusProvider ?: StatusProvider { false },
                        swipeCallback = swipeCallback,
                        moveCallback = moveCallback,
                        statusCallback = statusCallback
                )
        ).attachToRecyclerView(recyclerView)
    }

    private class SimpleMoveImpl(
            private val list: List<Any>,
            private val onMoved: (srcPosition: Int, targetPosition: Int) -> Unit
    ) : OnItemMoveCallback {
        override fun onMove(srcPosition: Int, targetPosition: Int): Boolean {
            val indices = list.indices
            return if (srcPosition in indices && targetPosition in indices) {
                Collections.swap(list, srcPosition, targetPosition)
                onMoved(srcPosition, targetPosition)
                true
            } else {
                false
            }
        }
    }

    private class SimpleSwipeImpl(
            private val list: MutableList<out Any>,
            private val onSwiped: (adapterPosition: Int) -> Unit
    ) : OnItemSwipeCallback {
        override fun onSwipe(adapterPosition: Int) {
            if (adapterPosition in list.indices) {
                list.removeAt(adapterPosition)
            }
            onSwiped(adapterPosition)
        }
    }

}

/**
 * 附加一个手指处理器
 */
fun RecyclerView.attachTouchHelper(): LItemTouchHelperBuilder {
    return LItemTouchHelperBuilder(this)
}