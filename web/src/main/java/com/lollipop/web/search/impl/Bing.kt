package com.lollipop.web.search.impl

import com.lollipop.web.search.SearchEngine
import com.lollipop.web.search.SearchEngineCallback

open class Bing: SearchEngine {

    override fun getSearchUrl(keyword: String, callback: SearchEngineCallback) {
        callback.onSearchEngineResult("https://bing.com/search?q=$keyword")
    }

}