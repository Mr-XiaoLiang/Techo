package com.lollipop.web.search

interface SearchEngine {

    fun getSearchUrl(keyword: String, callback: SearchEngineCallback)

}