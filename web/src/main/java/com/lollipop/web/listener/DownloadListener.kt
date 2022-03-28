package com.lollipop.web.listener

interface DownloadListener {

    fun createDownload(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimetype: String,
        contentLength: Long
    )

}