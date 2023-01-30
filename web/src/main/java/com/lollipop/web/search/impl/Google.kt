package com.lollipop.web.search.impl

import androidx.lifecycle.Lifecycle
import com.lollipop.web.search.*

open class Google(lifecycle: Lifecycle) : SimpleSearchEngine(lifecycle) {

    override fun getSearchUrl(keyword: String): String {
        return "https://www.google.com/search?q=${keyword}"
    }

}
