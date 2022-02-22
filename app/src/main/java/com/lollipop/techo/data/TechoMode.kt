package com.lollipop.techo.data

import android.content.Context
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import java.lang.ref.WeakReference

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
            override fun onInfoChanged(start: Int, count: Int) {
            }
        }

    }

    class Detail(
        listener: StateListener,
        context: Context
    ) : BaseMode(listener) {

        companion object {
            private const val NO_ID = 0
        }

        private val dbUtil = TechoDbUtil(context)

        val info = TechoInfo()

        private fun resetInfo() {
            info.id = NO_ID
            info.flag = TechoFlag()
            info.title = ""
            info.items.clear()
            initList()
        }

        fun loadOrCreate(id: Int) {
            if (id != NO_ID) {
                info.id = id
                load()
            } else {
                new()
            }
        }

        fun reset() {
            loadStart()
            resetInfo()
            infoChanged(0, info.items.size)
            loadEnd()
        }

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

        fun delete() {
            loadStart()
            doAsync {
                if (info.id != NO_ID) {
                    dbUtil.deleteTecho(info.id)
                }
                resetInfo()
                onUI {
                    infoChanged(0, info.items.size)
                    loadEnd()
                }
            }
        }

        fun new() {
            reset()
        }

        fun load() {
            loadStart()
            if (info.id == NO_ID) {
                resetInfo()
                infoChanged(0, info.items.size)
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
                onUI {
                    infoChanged(0, info.items.size)
                    loadEnd()
                }
            }
        }

        private fun initList() {
            if (info.items.isEmpty()) {
                info.items.add(TextItem())
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
                    infoChanged(start, count)
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

        private val listenerWrapper = WeakReference(listener)

        protected fun loadStart() {
            listenerWrapper.get()?.onLoadStart()
        }

        protected fun loadEnd() {
            listenerWrapper.get()?.onLoadEnd()
        }

        protected fun infoChanged(start: Int, count: Int) {
            listenerWrapper.get()?.onInfoChanged(start, count)
        }

    }

    interface StateListener {
        fun onLoadStart()
        fun onLoadEnd()
        fun onInfoChanged(start: Int, count: Int)
    }

}