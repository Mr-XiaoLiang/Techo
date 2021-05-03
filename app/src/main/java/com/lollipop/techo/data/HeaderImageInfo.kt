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

        /**
         * 完整的Url链接地址
         * （包含图片裁剪尺寸
         */
        val url by withThis("")

        /**
         * 基础的URL
         * （不包含图片剪裁尺寸
         */
        val urlBase by withThisByRename("urlbase", "")

        val copyright by withThis("")

    }

}