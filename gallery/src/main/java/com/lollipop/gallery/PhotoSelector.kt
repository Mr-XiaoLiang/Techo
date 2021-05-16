package com.lollipop.gallery

import android.content.Context

/**
 * @author lollipop
 * @date 2021/5/16 19:35
 */
class PhotoSelector(private val photoManager: PhotoManager) {

    private val selectedPhotoList = ArrayList<SelectedPhoto>()

    fun clear() {
        selectedPhotoList.clear()
    }

    fun refresh(context: Context) {
        photoManager.refresh(context)
    }

    val selectedCount: Int
        get() {
            return selectedPhotoList.size
        }

    fun interface OnSelectedChangeListener {
        fun onSelectedChange()
    }

    data class SelectedPhoto(
        val position: Int,
        val photo: Photo
    )

}