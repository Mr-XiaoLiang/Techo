package com.lollipop.techo.data

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.list.OnItemMoveCallback
import com.lollipop.base.list.OnItemSwipeCallback
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import java.lang.ref.WeakReference
import java.util.Collections

sealed class TechoMode private constructor(
    listener: Any,
    lifecycle: Lifecycle?,
    context: Context,
) {

    companion object {

        const val NO_ID = 0

        @SuppressLint("NotifyDataSetChanged")
        fun onInfoChangedDefaultImpl(
            adapter: RecyclerView.Adapter<*>?,
            first: Int,
            second: Int,
            type: ChangedType
        ) {
            adapter ?: return
            when (type) {
                ChangedType.Full -> {
                    adapter.notifyDataSetChanged()
                }

                ChangedType.Modify -> {
                    adapter.notifyItemRangeChanged(first, second)
                }

                ChangedType.Insert -> {
                    adapter.notifyItemRangeInserted(first, second)
                }

                ChangedType.Delete -> {
                    adapter.notifyItemRangeRemoved(first, second)
                }

                ChangedType.Move -> {
                    adapter.notifyItemMoved(first, second)
                    adapter.notifyItemChanged(first)
                    adapter.notifyItemChanged(second)
                }
            }
        }

        fun edit(context: Context): TechoModeBuilder.EditBuilder {
            return TechoModeBuilder.EditBuilder(context)
        }

        fun list(context: Context): TechoModeBuilder.ListBuilder {
            return TechoModeBuilder.ListBuilder(context)
        }

        fun detail(context: Context): TechoModeBuilder.DetailBuilder {
            return TechoModeBuilder.DetailBuilder(context)
        }
    }

    private val lifecycleEnable = lifecycle != null
    protected val dbUtil = TechoDbUtil(context)

    private val listenerWrapper = WeakReference(listener)
    private val lifecycleWrapper = WeakReference(lifecycle)

    private val isActive: Boolean
        get() {
            if (lifecycleEnable) {
                val currentState = lifecycleWrapper.get()?.currentState ?: return false
                return currentState.isAtLeast(Lifecycle.State.CREATED)
            } else {
                return true
            }
        }

    protected fun loadStart() {
        if (isActive) {
            optListener<LoadingStateListener>()?.onLoadStart()
        }
    }

    protected fun loadEnd() {
        if (isActive) {
            optListener<LoadingStateListener>()?.onLoadEnd()
        }
    }

    protected fun infoChanged(first: Int, second: Int, type: ChangedType) {
        if (isActive) {
            optListener<PartStateListener>()?.onInfoChanged(first, second, type)
        }
    }

    protected fun infoChanged(info: TechoInfo) {
        if (isActive) {
            optListener<DetailStateListener>()?.onInfoChanged(info)
        }
    }

    private inline fun <reified T> optListener(): T? {
        val any = listenerWrapper.get() ?: return null
        if (any is T) {
            return any
        }
        return null
    }

    protected fun formatData(itemList: List<TechoItem>): IntArray {
        var number = 1
        var updateStart = -1
        var updateEnd = -1
        itemList.forEachIndexed { index, info ->
            when (info) {
                is TechoItem.Number -> {
                    if (index < 0) {
                        updateStart = index
                    }
                    if (updateEnd < index) {
                        updateEnd = index
                    }
                    info.number = number
                    number++
                }

                is TechoItem.Split -> {
                    number = 1
                }

                is TechoItem.CheckBox -> {

                }

                is TechoItem.Photo -> {

                }

                is TechoItem.Text -> {

                }

                is TechoItem.Title -> {

                }

                is TechoItem.Recording -> {
                    // TODO()
                }

                is TechoItem.Vcr -> {
                    // TODO()
                }
            }
        }
        return intArrayOf(updateStart, updateEnd)
    }

    class EditMode(
        listener: EditStateListener,
        lifecycle: Lifecycle?,
        context: Context
    ) : TechoMode(listener, lifecycle, context), OnItemMoveCallback, OnItemSwipeCallback {

        val itemList = ArrayList<TechoItem>()

        var flag: TechoFlag = TechoFlag()

        var infoId = NO_ID
            private set

        val keyWords = ArrayList<String>()

        var createTime: Long = 0
            private set
        var updateTime: Long = 0
            private set

        /**
         * 当手帐中的item顺序被调整时触发
         */
        override fun onMove(srcPosition: Int, targetPosition: Int): Boolean {
            val list = itemList
            val indices = list.indices
            return if (srcPosition in indices && targetPosition in indices) {
                val srcItem = list[srcPosition]
                val targetItem = list[targetPosition]
                if (srcItem is TechoItem.Title || targetItem is TechoItem.Title) {
                    return false
                }
                if (srcItem is TechoItem.Number && targetItem is TechoItem.Number) {
                    val number = srcItem.number
                    srcItem.number = targetItem.number
                    targetItem.number = number
                }
                Collections.swap(list, srcPosition, targetPosition)
                infoChanged(srcPosition, targetPosition, ChangedType.Move)
                update()
                true
            } else {
                false
            }
        }

        /**
         * 当手帐中的item被移除时触发
         */
        override fun onSwipe(adapterPosition: Int) {
            val list = itemList
            if (adapterPosition in list.indices) {
                val item = list[adapterPosition]
                if (item is TechoItem.Title) {
                    infoChanged(adapterPosition, 1, ChangedType.Modify)
                    return
                }
                list.removeAt(adapterPosition)
                infoChanged(adapterPosition, 1, ChangedType.Delete)
                update()
            }
        }

        /**
         * 尝试加载一个手帐，如果加载失败，那么会创建一个新的
         */
        fun loadOrCreate(id: Int) {
            if (id != NO_ID) {
                infoId = id
                load()
            } else {
                new()
            }
        }

        /**
         * 加载手帐信息
         */
        private fun load() {
            loadStart()
            if (infoId == NO_ID) {
                resetInfo()
                infoChanged(0, itemList.size, ChangedType.Full)
                loadEnd()
                return
            }
            doAsync {
                val newInfo = dbUtil.selectTechoById(infoId)
                if (newInfo != null) {
                    flag = newInfo.flag
                    itemList.clear()
                    itemList.add(TechoItem.Title().apply {
                        value = newInfo.title
                    })
                    itemList.addAll(newInfo.items)
                    keyWords.addAll(newInfo.keyWords)
                    createTime = newInfo.createTime
                    updateTime = newInfo.updateTime
                } else {
                    resetInfo()
                }
                initList()
                formatData()
                onUI {
                    infoChanged(0, itemList.size, ChangedType.Full)
                    loadEnd()
                }
            }
        }

        /**
         * 创建一个新的手帐
         */
        private fun new() {
            reset()
        }

        /**
         * 重置
         * 将会重置所有数据为新建状态
         */
        private fun reset() {
            loadStart()
            resetInfo()
            infoChanged(0, itemList.size, ChangedType.Full)
            loadEnd()
        }

        private fun resetInfo() {
            infoId = NO_ID
            flag = TechoFlag()
            itemList.clear()
            keyWords.clear()
            createTime = 0
            updateTime = 0
            initList()
        }

        private fun initList() {
            if (itemList.isEmpty()) {
                itemList.add(TechoItem.Title())
                itemList.add(TechoItem.Text())
            }
        }

        private fun formatData(): IntArray {
            return formatData(itemList)
        }

        /**
         * 更新
         * 将会把当前数据更新并同步到数据库
         */
        fun update(callback: (() -> Unit)? = null) {
            loadStart()
            doAsync {
                val info = TechoInfo()
                itemList.forEach {
                    if (it is TechoItem.Title) {
                        info.title = it.value
                    } else {
                        info.items.add(it)
                    }
                }
                info.flag = flag
                val now = System.currentTimeMillis()
                updateTime = now
                if (infoId == NO_ID) {
                    createTime = now
                }
                info.updateTime = updateTime
                info.createTime = createTime
                if (infoId == NO_ID) {
                    // 插入后更新id
                    infoId = dbUtil.insertTecho(info)
                } else {
                    dbUtil.updateTecho(info)
                }
                onUI {
                    callback?.invoke()
                    loadEnd()
                }
            }
        }

        /**
         * 对所有内容进行格式化
         * 主要是针对带有序号的项进行序号重排
         */
        fun format() {
            loadStart()
            doAsync {
                val formatResult = formatData()
                onUI {
                    notifyInfoFormatChanged(formatResult)
                    loadEnd()
                }
            }
        }

        private fun notifyInfoFormatChanged(formatResult: IntArray) {
            val start = formatResult[0]
            val count = formatResult[1] - start + 1
            if (start < 0 || start >= itemList.size || count < 1) {
                return
            }
            infoChanged(start, count, ChangedType.Modify)
        }

        /**
         * 插入一个新的内容
         * 这个内容是空的
         */
        fun insert(type: TechoItemType) {
            val newItem = TechoItem.createItem(type)
            val start = itemList.size
            itemList.add(newItem)
            infoChanged(start, 1, ChangedType.Insert)
            update()
            format()
        }

        /**
         * 内容变化时的响应操作的默认实现
         */
        fun onInfoChangedDefaultImpl(
            adapter: RecyclerView.Adapter<*>?,
            first: Int,
            second: Int,
            type: ChangedType
        ) {
            TechoMode.onInfoChangedDefaultImpl(adapter, first, second, type)
        }

    }

    class DetailMode(
        listener: DetailStateListener,
        lifecycle: Lifecycle?,
        context: Context,
    ) : TechoMode(listener, lifecycle, context) {

        /**
         * 手帐详情的主体
         */
        val info = TechoInfo()

        private fun resetInfo() {
            info.id = NO_ID
            info.flag = TechoFlag()
            info.title = ""
            info.items.clear()
        }

        /**
         * 尝试加载一个手帐，如果加载失败，那么会创建一个新的
         */
        fun load(id: Int) {
            info.id = id
            load()
        }

        private fun formatData(): IntArray {
            return formatData(info.items)
        }

        /**
         * 删除当前整个手帐本身
         */
        fun delete() {
            loadStart()
            doAsync {
                if (info.id != NO_ID) {
                    dbUtil.deleteTecho(info.id)
                }
                resetInfo()
                onUI {
                    infoChanged(info)
                    loadEnd()
                }
            }
        }

        /**
         * 加载手帐信息
         */
        private fun load() {
            loadStart()
            if (info.id == NO_ID) {
                resetInfo()
                infoChanged(info)
                loadEnd()
                return
            }
            doAsync {
                val newInfo = dbUtil.selectTechoById(info.id)
                if (newInfo != null) {
                    info.flag = newInfo.flag
                    info.items.clear()
                    info.items.addAll(newInfo.items)
                    info.title = newInfo.title
                } else {
                    resetInfo()
                }
                formatData()
                onUI {
                    infoChanged(info)
                    loadEnd()
                }
            }
        }
    }

    class ListMode(
        listener: ListStateListener,
        lifecycle: Lifecycle?,
        context: Context
    ) : TechoMode(listener, lifecycle, context), OnItemSwipeCallback {

        companion object {
            private const val DEFAULT_PAGE_INDEX = 0
        }

        val info = ArrayList<TechoInfo>()

        private var pageIndex = DEFAULT_PAGE_INDEX

        private val pageSize = 20

        private var keyword = ""

        fun loadNext() {
            loadStart()
            val key = keyword
            doAsync {
                val newList = if (key.isEmpty()) {
                    dbUtil.selectTecho(pageIndex, pageSize)
                } else {
                    dbUtil.selectTechoByKeyword(key, pageIndex, pageSize)
                }
                pageIndex++
                val start = info.size
                val count = newList.size
                info.addAll(newList)
                onUI {
                    infoChanged(start, count, ChangedType.Insert)
                    loadEnd()
                }
            }
        }

        fun onNewInsert(id: Int) {
            if (id == NO_ID) {
                return
            }
            loadStart()
            doAsync {
                val techo = dbUtil.selectTechoById(id)
                var itemPosition = -1
                var isInsert = false
                if (techo != null) {
                    for (index in info.indices) {
                        if (info[index].id == id) {
                            itemPosition = index
                            info[index] = techo
                            break
                        }
                    }
                    if (itemPosition < 0) {
                        info.add(0, techo)
                        itemPosition = 0
                        isInsert = true
                    }
                }
                onUI {
                    if (itemPosition >= 0) {
                        infoChanged(
                            itemPosition,
                            1,
                            if (isInsert) {
                                ChangedType.Insert
                            } else {
                                ChangedType.Modify
                            }
                        )
                    }
                    loadEnd()
                }
            }
        }

        fun refresh() {
            this.keyword = ""
            pageIndex = DEFAULT_PAGE_INDEX
            loadNext()
        }

        fun search(key: String) {
            this.keyword = key
            pageIndex = DEFAULT_PAGE_INDEX
            loadNext()
        }

        override fun onSwipe(adapterPosition: Int) {
            if (adapterPosition in info.indices) {
                val item = info[adapterPosition]
                info.removeAt(adapterPosition)
                doAsync {
                    dbUtil.deleteTecho(item.id)
                }
                infoChanged(adapterPosition, 1, ChangedType.Delete)
            }
        }

    }

    interface LoadingStateListener {
        fun onLoadStart()
        fun onLoadEnd()
    }

    interface PartStateListener {

        fun onInfoChanged(first: Int, second: Int, type: ChangedType)

    }

    interface DetailStateListener : LoadingStateListener {
        fun onInfoChanged(info: TechoInfo)
    }

    interface EditStateListener : LoadingStateListener, PartStateListener

    interface ListStateListener : LoadingStateListener, PartStateListener

    enum class ChangedType {
        Full,
        Modify,
        Insert,
        Delete,
        Move
    }

}