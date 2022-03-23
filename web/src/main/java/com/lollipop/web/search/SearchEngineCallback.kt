package com.lollipop.web.search

interface SearchEngineCallback {

    fun onSearchEngineResult(url: String)

    fun onSearchRelevantResult(values: List<String>)

}