package com.lollipop.web.search.impl

import com.lollipop.web.search.SearchEngine
import com.lollipop.web.search.SearchEngineCallback

open class Google: SearchEngine {

    override fun getSearchUrl(keyword: String, callback: SearchEngineCallback) {
        callback.onSearchEngineResult("https://www.google.com/search?q=${keyword}")
    }

}