package com.lollipop.web.completion.impl

import com.lollipop.web.completion.UrlCompletion
import com.lollipop.web.completion.UrlCompletionResult

class EmptyCompletion : UrlCompletion {
    override fun complement(url: String, result: UrlCompletionResult) {
        result.onCompletionResult(url)
    }
}