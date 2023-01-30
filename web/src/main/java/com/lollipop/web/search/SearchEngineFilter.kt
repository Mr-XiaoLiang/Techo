package com.lollipop.web.search

interface SearchEngineFilter {

    fun onSearch(keyword: String): List<SearchSuggestion>

}