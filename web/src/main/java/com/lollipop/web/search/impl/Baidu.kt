package com.lollipop.web.search.impl

import com.lollipop.web.search.SearchEngine
import com.lollipop.web.search.SearchEngineCallback

open class Baidu: SearchEngine {

    override fun getSearchUrl(keyword: String, callback: SearchEngineCallback) {
        callback.onSearchEngineResult("https://www.baidu.com/s?ie=utf-8&wd=$keyword")
    }

}