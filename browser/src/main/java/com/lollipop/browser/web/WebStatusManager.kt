package com.lollipop.browser.web

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * 网页的状态管理器
 */
object WebStatusManager {

    private val statusFile: File? = null

    private val statusArray = ArrayList<WebStatus>()
    private val statusMap = HashMap<String, WebStatus>()

    fun resume(context: Context) {
        // TODO 读取并且初始化状态管理区
    }

    fun save() {
        // TODO 保存状态到本地
    }

    fun newPage(): String {
        var pageId = ""
        var count = 0
        do {
            pageId = createPageId()
            count++
            if (count > 10) {
                throw RuntimeException("cant create page id with UUID")
            }
        } while (statusMap.containsKey(pageId))
        addPage(pageId)
        return pageId
    }

    private fun addPage(pageId: String) {
        val webStatus = WebStatus(pageId = pageId)
        statusArray.add(webStatus)
        statusMap[pageId] = webStatus
    }

    fun removePage(pageId: String) {
        val status = statusMap[pageId] ?: return
        statusArray.remove(status)
        removePageCache(status)
    }

    private fun removePageCache(status: WebStatus) {
        status.favicon?.tryDelete()
        status.snapshot?.tryDelete()
        // TODO
    }

    private fun File.tryDelete() {
        try {
            delete()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun createPageId(): String {
        return UUID.randomUUID().toString()
    }

    class WebStatus(
        /**
         * 页面的ID，这是唯一标识符，用来区分每个Tab下的浏览器窗口
         */
        val pageId: String,
        /**
         * 页面标题
         */
        var title: String = "",
        /**
         * 页面URL
         */
        var url: String = "",
        /**
         * 页面的ICON
         */
        var favicon: File? = null,
        /**
         * 页面快照
         */
        var snapshot: File? = null,
        /**
         * 加载状态
         */
        var progress: Float = 0F
    ) {

        companion object {

            private const val PAGE_ID = "pageId"
            private const val TITLE = "title"
            private const val URL = "url"
            private const val FAVICON = "favicon"
            private const val SNAPSHOT = "snapshot"

            private fun String.optFile(): File? {
                if (this.isEmpty()) {
                    return null
                }
                val file = File(this)
                if (!file.exists()) {
                    return null
                }
                return file
            }

            fun fromJson(json: JSONObject): WebStatus {
                return WebStatus(
                    pageId = json.optString(PAGE_ID),
                    title = json.optString(TITLE),
                    url = json.optString(URL),
                    favicon = json.optString(FAVICON).optFile(),
                    snapshot = json.optString(SNAPSHOT).optFile(),
                )
            }
        }

        fun toJson(): JSONObject {
            return JSONObject().apply {
                put(PAGE_ID, pageId)
                put(TITLE, title)
                put(URL, url)
                put(FAVICON, favicon?.path ?: "")
                put(SNAPSHOT, snapshot?.path ?: "")
            }
        }

    }

}