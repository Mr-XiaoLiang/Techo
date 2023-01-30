package com.lollipop.web.search

import java.lang.ref.WeakReference

class SearchMission private constructor(
    val keyword: String,
    private val callbackReference: WeakReference<SearchEngineCallback>
): SearchCancelable {

    companion object {
        fun create(keyword: String, callback: SearchEngineCallback): SearchMission {
            return SearchMission(keyword, WeakReference(callback))
        }
    }

    /**
     * 搜索任务是否已取消
     */
    var isCancelled = false
        private set

    override fun cancel() {
        isCancelled = true
    }

    internal fun onSearchRelevantResult(values: List<SearchSuggestion>) {
        callbackReference.get()?.onSearchRelevantResult(values)
    }

}