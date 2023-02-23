package com.lollipop.web.completion.impl

import android.net.Uri
import com.lollipop.web.completion.UrlCompletion
import com.lollipop.web.completion.UrlCompletionResult

class EmptyCompletion : UrlCompletion {

    companion object {
        private const val HTTP = "http"
        private const val HTTPS = "https"
    }

    override fun complement(url: String, result: UrlCompletionResult) {
        if (isUrl(url)) {
            result.onLoadUrl(fixScheme(url))
        } else {
            result.onSearch(url)
        }
    }

    private fun fixScheme(url: String): String {
        val scheme = Uri.parse(url).scheme ?: ""
        if (scheme.isEmpty()) {
            return "https://$url"
        }
        if (scheme.equals(HTTP, ignoreCase = true) || scheme.equals(HTTPS, ignoreCase = true)) {
            return url
        }
        return "https://$url"
    }

    private fun isUrl(url: String): Boolean {
        return url.indexOf(".") > 0
    }

}