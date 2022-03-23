package com.lollipop.web.completion

interface UrlCompletionResult {

    fun onLoadUrl(url: String)

    fun onSearch(keyword: String)

}