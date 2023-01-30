package com.lollipop.web.search

import androidx.lifecycle.Lifecycle

abstract class SimpleSearchEngine(lifecycle: Lifecycle) : SearchEngine(lifecycle) {

    override fun onSearch(keyword: String): List<SearchSuggestion> {
        return listOf(
            SearchSuggestion(
                name = "",
                url = getSearchUrl(keyword),
                type = SearchSuggestionType.SEARCH,
                score = 80
            )
        )
    }

    abstract fun getSearchUrl(keyword: String): String

}