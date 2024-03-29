package com.lollipop.gallery

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.database.getStringOrNull
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


/**
 * @author lollipop
 * @date 2021/5/10 22:34
 * 相片管理器
 * 提供了图片的简单查询功能和保存功能
 */
class PhotoManager private constructor(
    private val dataList: ArrayList<Photo> = ArrayList()
) : List<Photo> by dataList {

    companion object {
        fun getPermissions(): Array<String> {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                    return arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    )
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    return arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                }

                else -> {
                    return arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }

        private const val PHOTO_DIR = "my_photo"

        fun create(): PhotoManager {
            return PhotoManager()
        }

        private fun getLocalPhotoDir(context: Context): File {
            return File(context.filesDir, PHOTO_DIR).apply {
                if (!isDirectory) {
                    this.delete()
                }
                if (!this.exists()) {
                    this.mkdirs()
                }
            }
        }

        /**
         * 本地读取一张照片
         * 如果这张照片是空的，那么返回为null
         */
        fun readLocal(context: Context, name: String): File? {
            val photoFile = getPhotoFile(context, name)
            if (!photoFile.exists() || photoFile.isDirectory) {
                return null
            }
            return photoFile
        }

        private fun getPhotoFile(context: Context, name: String): File {
            return File(getLocalPhotoDir(context), name)
        }

        /**
         * 保存一张照片到本地
         */
        fun save(context: Context, photo: Photo): File? {
            context.contentResolver
                .openFileDescriptor(photo.uri, "r")
                ?.use { descriptor ->
                    val fileDescriptor = descriptor.fileDescriptor
                    val fileInputStream = FileInputStream(fileDescriptor)
                    val photoFile = getPhotoFile(context, photo.title)
                    if (photoFile.isDirectory || photoFile.exists()) {
                        photoFile.delete()
                    }
                    photoFile.createNewFile()
                    val fileOutputStream = FileOutputStream(photoFile)
                    val buffer = ByteArray(1024 * 2)
                    do {
                        val length = fileInputStream.read(buffer)
                        if (length >= 0) {
                            fileOutputStream.write(buffer, 0, length)
                        }
                    } while (length >= 0)
                    fileOutputStream.flush()
                    fileOutputStream.close()
                    fileInputStream.close()
                    return photoFile
                }
            return null
        }

        fun checkPermission(context: Context): Boolean {
            val permissions = getPermissions()
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        context, permission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    return true
                }
            }
            return false
        }

        fun checkPermission(map: Map<String, Boolean>): Boolean {
            val permissions = getPermissions()
            for (permission in permissions) {
                if (map[permission] == true) {
                    return true
                }
            }
            return false
        }
    }

    var isMediaStoreChanged = true

    fun shouldShowPermissionRationale(activity: AppCompatActivity): Boolean {
        val permissions = getPermissions()
        for (permission in permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true
            }
        }
        return false
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

    fun registerMediaChangeListener() {
        // TODO
    }

    fun unregisterMediaChangeListener() {
        // TODO
    }

}