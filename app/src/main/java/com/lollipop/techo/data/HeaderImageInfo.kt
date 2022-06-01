package com.lollipop.techo.data

import com.lollipop.techo.data.json.JsonInfo
import org.json.JSONObject

/**
 * @author lollipop
 * @date 5/1/21 22:40
 * 顶部图片的数据结构体
 */
class HeaderImageInfo : JsonInfo {

    companion object {
        private const val KEY_IMAGES = "images"
        private const val KEY_URL = "url"
        private const val KEY_URL_BASE = "urlbase"
        private const val KEY_COPYRIGHT = "copyright"
        private const val KEY_COPYRIGHT_LINK = "copyrightlink"
        private const val KEY_TITLE = "title"
    }

    /**
     * 完整的Url链接地址
     * （包含图片裁剪尺寸
     */
    var url = ""

    /**
     * 基础的URL
     * （不包含图片剪裁尺寸
     */
    var urlBase = ""

    /**
     * 版权的描述信息
     */
    var copyright = ""

    /**
     * 版权的描述信息的链接
     */
    var copyrightLink = ""

    /**
     * 标题信息
     */
    var title = ""

    /**
     * 获取完整的URL链接地址
     */
    val fullUrl by lazy {
        RequestService.formatFullImageUrl(url)
    }

    override fun toJson(): JSONObject {
        return JSONObject().apply {
            put(KEY_URL, url)
            put(KEY_URL_BASE, urlBase)
            put(KEY_COPYRIGHT, copyright)
            put(KEY_COPYRIGHT_LINK, copyrightLink)
            put(KEY_TITLE, title)
        }
    }

    override fun parse(json: JSONObject) {
        val imagesArray = json.optJSONArray(KEY_IMAGES)
        if (imagesArray != null && imagesArray.length() > 0) {
            val obj = imagesArray.optJSONObject(0)
            if (obj != null) {
                parseInfo(obj)
                return
            }
        }
        parseInfo(json)
    }

    private fun parseInfo(json: JSONObject) {
        url = json.optString(KEY_URL)
        urlBase = json.optString(KEY_URL_BASE)
        copyright = json.optString(KEY_COPYRIGHT)
        copyrightLink = json.optString(KEY_COPYRIGHT_LINK)
        title = json.optString(KEY_TITLE)
    }

}