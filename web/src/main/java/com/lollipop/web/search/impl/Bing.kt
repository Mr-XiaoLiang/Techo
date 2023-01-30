package com.lollipop.web.search.impl

import androidx.lifecycle.Lifecycle
import com.lollipop.web.search.*

open class Bing(lifecycle: Lifecycle) : SimpleSearchEngine(lifecycle) {

    override fun getSearchUrl(keyword: String): String {
        return "https://bing.com/search?q=$keyword"
    }

}
