package com.lollipop.techo.data

import android.content.Context
import com.lollipop.base.list.OnItemMoveCallback
import com.lollipop.base.list.OnItemSwipeCallback
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

object TechoMode {

    fun create(context: Context): Builder {
        return Builder(context)
    }

    class Builder(private val context: Context) {
        private var listener: StateListener? = null

        fun attach(listener: StateListener): Builder {
            this.listener = listener
            return this
        }

        fun buildDetailMode(): Detail {
            val lis = listener ?: EmptyListener()
            return Detail(lis, context)
        }

        fun buildListMode(): List {
            val lis = listener ?: EmptyListener()
            return List(lis, context)
        }

        private class EmptyListener : StateListener {
            override fun onLoadStart() {
            }
            override fun onLoadEnd() {
            }
            override fun onInfoChanged(start: Int, count: Int, type: ChangedType) {
            }
        }

    }

    class Detail(
        listener: StateListener,
        context: Context
    ) : BaseMode(listener), OnItemMoveCallback, OnItemSwipeCallback {

        private val dbUtil = TechoDbUtil(context)

        /**
         * 手帐详情的主体
         */
        val info = TechoInfo()

        private fun resetInfo() {
            info.id = NO_ID
            info.flag = TechoFlag()
            info.title = ""
            info.items.clear()
            initList()
        }

        /**
         * 尝试加载一个手帐，如果加载失败，那么会创建一个新的
         */
        fun loadOrCreate(id: Int) {
            if (id != NO_ID) {
                info.id = id
                load()
            } else {
                new()
            }
        }

        /**
         * 插入一个新的内容
         * 这个内容是空的
         */
        fun insert(type: TechoItemType) {
            val newItem = when (type) {
                TechoItemType.Empty -> {
                    return
                }
                TechoItemType.Text -> {
                    TextItem()
                }
                TechoItemType.Number -> {
                    NumberItem()
                }
                TechoItemType.CheckBox -> {
                    CheckBoxItem()
                }
                TechoItemType.Photo -> {
                    PhotoItem()
                }
                TechoItemType.Split -> {
                    SplitItem()
                }
            }
            val start = info.items.size
            info.items.add(newItem)
            infoChanged(start, 1, ChangedType.Insert)
            update()
        }

        /**
         * 修改一个内容
         */
        fun modify(item: BaseTechoItem) {
            val index = info.items.indexOf(item)
            if (index >= 0) {
                infoChanged(index, 1, ChangedType.Modify)
                update()
            }
        }

        /**
         * 对所有内容进行格式化
         * 主要是针对带有序号的项进行序号重排
         */
        fun format() {
            loadStart()
            doAsync {
                formatData()
                onUI {
                    loadEnd()
                }
            }
        }

        private fun formatData() {
            var number = 1
            info.items.forEach {
                when (it) {
                    is NumberItem -> {
                        it.number = number
                        number++
                    }
                    is SplitItem -> {
                        number = 1
                    }
                }
            }
        }

        /**
         * 重置
         * 将会重置所有数据为新建状态
         */
        fun reset() {
            loadStart()
            resetInfo()
            infoChanged(0, info.items.size, ChangedType.Full)
            loadEnd()
        }

        /**
         * 更新
         * 将会把当前数据更新并同步到数据库
         */
        fun update() {
            loadStart()
            doAsync {
                if (info.id == NO_ID) {
                    // 插入后更新id
                    info.id = dbUtil.insertTecho(info)
                } else {
                    dbUtil.updateTecho(info)
                }
                onUI {
                    loadEnd()
                }
            }
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
                    infoChanged(0, info.items.size, ChangedType.Delete)
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
         * 加载手帐信息
         */
        private fun load() {
            loadStart()
            if (info.id == NO_ID) {
                resetInfo()
                infoChanged(0, info.items.size, ChangedType.Full)
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
                initList()
                formatData()
                onUI {
                    infoChanged(0, info.items.size, ChangedType.Full)
                    loadEnd()
                }
            }
        }

        private fun initList() {
            if (info.items.isEmpty()) {
                info.items.add(TextItem())
            }
        }

        /**
         * 当手帐中的item顺序被调整时触发
         */
        override fun onMove(srcPosition: Int, targetPosition: Int): Boolean {
            val indices = info.items.indices
            return if (srcPosition in indices && targetPosition in indices) {
                Collections.swap(info.items, srcPosition, targetPosition)
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
            if (adapterPosition in info.items.indices) {
                info.items.removeAt(adapterPosition)
                infoChanged(adapterPosition, 1, ChangedType.Delete)
                update()
            }
        }

    }

    class List(
        listener: StateListener,
        context: Context
    ) : BaseMode(listener) {

        companion object {
            private const val DEFAULT_PAGE_INDEX = 0
        }

        val info = ArrayList<TechoInfo>()

        private var pageIndex = DEFAULT_PAGE_INDEX

        private val pageSize = 20

        private val dbUtil = TechoDbUtil(context)

        fun loadNext() {
            loadStart()
            doAsync {
                val newList = dbUtil.selectTecho(pageIndex, pageSize)
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
                var isInsert = false
                if (techo != null) {
                    info.add(0, techo)
                    isInsert = true
                }
                onUI {
                    if (isInsert) {
                        infoChanged(0, 1, ChangedType.Insert)
                    }
                    loadEnd()
                }
            }
        }

        fun refresh() {
            pageIndex = DEFAULT_PAGE_INDEX
            loadNext()
        }

    }

    open class BaseMode(
        listener: StateListener
    ) {

        companion object {
            const val NO_ID = 0
        }

        private val listenerWrapper = WeakReference(listener)

        protected fun loadStart() {
            listenerWrapper.get()?.onLoadStart()
        }

        protected fun loadEnd() {
            listenerWrapper.get()?.onLoadEnd()
        }

        protected fun infoChanged(start: Int, count: Int, type: ChangedType) {
            listenerWrapper.get()?.onInfoChanged(start, count, type)
        }

    }

    interface StateListener {
        fun onLoadStart()
        fun onLoadEnd()
        fun onInfoChanged(start: Int, count: Int, type: ChangedType)
    }

    enum class ChangedType {
        Full,
        Modify,
        Insert,
        Delete,
        Move
    }

}