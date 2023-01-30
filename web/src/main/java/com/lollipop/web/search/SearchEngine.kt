package com.lollipop.web.search

import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.lang.ref.WeakReference
import java.util.concurrent.Executor

abstract class SearchEngine(lifecycle: Lifecycle) {

    private val searchEngineFilterList = ArrayList<SearchEngineFilter>()

    private val executor = SingleExecutor(WeakReference(lifecycle), "SearchEngineHandler")

    fun addFilter(filter: SearchEngineFilter) {
        if (searchEngineFilterList.contains(filter)) {
            return
        }
        searchEngineFilterList.add(filter)
    }

    /**
     * 这是一个耗时的过程，因此在搜索的联想过程中
     * 请保持在子线程中执行
     */
    fun search(keyword: String, callback: SearchEngineCallback): SearchCancelable {
        val mission = SearchMission.create(keyword, callback)
        executor.execute(SearchTask(mission, ArrayList(searchEngineFilterList), ::onSearch))
        return mission
    }

    protected abstract fun onSearch(keyword: String): List<SearchSuggestion>

    fun destroy() {
        executor.destroy()
    }

    private class SearchTask(
        private val mission: SearchMission,
        private val filterList: List<SearchEngineFilter>,
        private val finallySearch: (keyword: String) -> List<SearchSuggestion>
    ) : Runnable {
        override fun run() {
            val resultList = ArrayList<SearchSuggestion>()
            try {
                val keyword = mission.keyword
                for (filter in filterList) {
                    if (mission.isCancelled) {
                        return
                    }
                    try {
                        resultList.addAll(filter.onSearch(keyword))
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
                if (mission.isCancelled) {
                    return
                }
                try {
                    resultList.addAll(finallySearch(keyword))
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                if (mission.isCancelled) {
                    return
                }
                resultList.sortByDescending { it.score }
                if (mission.isCancelled) {
                    return
                }
                mission.onSearchRelevantResult(resultList)
            } catch (e: Throwable) {
                e.printStackTrace()
                if (!mission.isCancelled) {
                    mission.onSearchRelevantResult(resultList)
                }
            }
        }

    }

    private class SingleExecutor(
        private val lifecycle: WeakReference<Lifecycle>,
        threadName: String
    ) : Executor {

        private val thread = HandlerThread(threadName).apply {
            start()
        }

        private val handler = Handler(thread.looper)

        var isDestroy = false
            private set

        private val eventObserver = LifecycleEventObserver { source, event ->
            if (event.targetState == Lifecycle.State.DESTROYED) {
                destroy()
            }
        }

        init {
            lifecycle.get()?.addObserver(eventObserver)
        }

        override fun execute(command: Runnable?) {
            if (isDestroy) {
                return
            }
            command ?: return
            handler.post(command)
        }

        fun destroy() {
            if (isDestroy) {
                return
            }
            isDestroy = true
            thread.quitSafely()
            lifecycle.get()?.removeObserver(eventObserver)
            lifecycle.clear()
        }
    }

}