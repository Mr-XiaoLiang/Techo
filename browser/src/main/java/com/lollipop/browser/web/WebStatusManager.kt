package com.lollipop.browser.web

import android.content.Context
import com.lollipop.base.util.ListenerManager
import com.lollipop.base.util.TxtHelper
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * 网页的状态管理器
 */
object WebStatusManager {

    private var statusFile: File? = null

    private const val STATUS_FILE_NAME = "status.ltf"

    private val statusArray = ArrayList<WebStatus>()
    private val statusMap = HashMap<String, WebStatus>()

    private val readyListener = ListenerManager<OnManagerReadyListener>()

    var isReady = false
        private set

    fun resume(context: Context) {
        init(context)
        loadData()
    }

    private fun init(context: Context) {
        statusFile = File(context.filesDir, STATUS_FILE_NAME)
    }

    private fun loadData() {
        doAsync {
            if (loadDataFromFile()) {
                onUI {
                    onReady()
                }
            }
        }
    }

    private fun loadDataFromFile(): Boolean {
        try {
            val file = statusFile ?: return false
            if (!file.exists()) {
                clearStatus()
                return true
            }
            val value = TxtHelper.readFromFile(file)
            val jsonArray = JSONArray(value)
            val list = ArrayList<WebStatus>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                try {
                    val status = WebStatus.fromJson(obj)
                    if (status.pageId.isNotEmpty()) {
                        list.add(status)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            resetStatus(list)
        } catch (e: Throwable) {
            e.printStackTrace()
            clearStatus()
        }
        return true
    }

    private fun clearStatus() {
        synchronized(WebStatusManager) {
            statusArray.clear()
            statusMap.clear()
        }
    }

    private fun resetStatus(list: List<WebStatus>) {
        synchronized(WebStatusManager) {
            statusArray.clear()
            statusMap.clear()
            list.forEach {
                statusArray.add(it)
                statusMap[it.pageId] = it
            }
        }
    }

    private fun onReady() {
        synchronized(WebStatusManager) {
            isReady = true
            readyListener.invoke { it.onStatusManagerReady() }
            readyListener.clear()
        }
    }

    fun save() {
        val file = statusFile ?: return
        doAsync {
            val tempList = ArrayList(statusArray)
            val jsonArray = JSONArray()
            tempList.forEach {
                if (it.pageId.isNotEmpty()) {
                    jsonArray.put(it.toJson())
                }
            }
            if (file.exists()) {
                file.delete()
            }
            TxtHelper.writeToFile(jsonArray.toString(), file)
        }
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
        synchronized(WebStatusManager) {
            statusArray.add(webStatus)
            statusMap[pageId] = webStatus
        }
    }

    fun removePage(pageId: String) {
        synchronized(WebStatusManager) {
            val status = statusMap[pageId] ?: return
            statusArray.remove(status)
            removePageCache(status)
        }
    }

    private fun removePageCache(status: WebStatus) {
        status.favicon?.tryDelete()
        status.snapshot?.tryDelete()
        // TODO
    }

    fun addReadyListener(listener: OnManagerReadyListener) {
        synchronized(WebStatusManager) {
            if (isReady) {
                listener.onStatusManagerReady()
                if (!readyListener.isEmpty) {
                    readyListener.invoke { it.onStatusManagerReady() }
                    readyListener.clear()
                }
                return
            }
            readyListener.addListener(listener)
        }
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

    fun interface OnManagerReadyListener {
        fun onStatusManagerReady()
    }

}