package com.lollipop.gallery

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.core.database.getStringOrNull


/**
 * @author lollipop
 * @date 2021/5/10 22:34
 * 相片管理器
 * 提供了图片的简单查询功能和保存功能
 */
class PhotoManager {

    private val dataList = ArrayList<Photo>()

    /**
     * 刷新相册集合数据
     */
    fun refresh(context: Context) {
        try {
            val order = MediaStore.MediaColumns.DATE_ADDED + " DESC "
            val cursor = context.contentResolver
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, order)
            dataList.clear()
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val idIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                    if (idIndex < 0) {
                        continue
                    }
                    val id: Long = cursor.getLong(idIndex)
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    val titleIndex = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)
                    val title = if (titleIndex < 0) {
                        ""
                    } else {
                        cursor.getStringOrNull(titleIndex)?:""
                    }
                    dataList.add(Photo(uri, title))
                }
                cursor.close()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    val size: Int
        get() {
            return dataList.size
        }

}