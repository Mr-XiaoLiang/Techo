package com.lollipop.techo.data

import com.lollipop.techo.data.json.JsonObjectInfo

/**
 * @author lollipop
 * @date 5/1/21 22:40
 * 顶部图片的数据结构体
 */
class HeaderImageInfo: JsonObjectInfo() {

    val images by arrayWithThis<ImageInfo>()

    class ImageInfo: JsonObjectInfo() {

        val url by withThis("")

    }

}