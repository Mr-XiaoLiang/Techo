package com.lollipop.web.search.impl

import androidx.lifecycle.Lifecycle
import com.lollipop.web.search.SearchEngine
import com.lollipop.web.search.SearchSuggestion
import com.lollipop.web.search.SearchSuggestionType
import com.lollipop.web.search.SimpleSearchEngine

open class Baidu(lifecycle: Lifecycle) : SimpleSearchEngine(lifecycle) {

    override fun getSearchUrl(keyword: String): String {
        return "https://www.baidu.com/s?ie=utf-8&wd=$keyword"
    }

}