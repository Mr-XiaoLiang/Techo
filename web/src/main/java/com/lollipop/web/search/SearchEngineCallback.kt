package com.lollipop.web.search

interface SearchEngineCallback {

    /**
     * 搜索引擎的建议结果
     */
    fun onSearchRelevantResult(values: List<SearchSuggestion>)

}