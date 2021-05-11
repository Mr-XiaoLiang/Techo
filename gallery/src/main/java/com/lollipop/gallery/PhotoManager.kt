package com.lollipop.gallery

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.database.getStringOrNull


/**
 * @author lollipop
 * @date 2021/5/10 22:34
 * 相片管理器
 * 提供了图片的简单查询功能和保存功能
 */
class PhotoManager {

    companion object {
        private const val READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val dataList = ArrayList<Photo>()

    fun checkPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, READ_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldShowPermissionRationale(activity: AppCompatActivity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, READ_PERMISSION)
    }

    fun requestPermission(
        activity: AppCompatActivity,
        showRationale: (String, () -> Unit) -> Unit,
        callback: (PhotoManager) -> Unit) {
        if (shouldShowPermissionRationale(activity)) {
            showRationale(READ_PERMISSION) {

            }
        } else {

        }
    }

    private fun requestPermission(activity: AppCompatActivity) {
//        prepareCall(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (isGranted) {
//            } else {
//            }
//        }
    }

    /**
     * 刷新相册集合数据
     */
    fun refresh(context: Context) {
        try {
            val order = MediaStore.MediaColumns.DATE_MODIFIED + " DESC "
            val cursor = context.contentResolver
                .query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    "${MediaStore.Images.Media.MIME_TYPE} = ? or ${MediaStore.Images.Media.MIME_TYPE} = ?",
                    arrayOf("image/jpeg", "image/png"), order
                )
            dataList.clear()
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val idIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                    if (idIndex < 0) {
                        continue
                    }
                    val id: Long = cursor.getLong(idIndex)
                    val uri =
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    val titleIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    val title = if (titleIndex < 0) {
                        ""
                    } else {
                        cursor.getStringOrNull(titleIndex) ?: ""
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

    operator fun get(index: Int): Photo {
        return dataList[index]
    }

    class TempActivity: AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
        }
    }

}