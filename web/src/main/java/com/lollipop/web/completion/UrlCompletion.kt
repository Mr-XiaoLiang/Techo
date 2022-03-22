package com.lollipop.web.completion

fun interface UrlCompletion {

    fun complement(url: String, result: UrlCompletionResult)

}