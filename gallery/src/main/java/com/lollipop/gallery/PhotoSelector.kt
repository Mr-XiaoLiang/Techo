package com.lollipop.gallery

import android.content.Context
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author lollipop
 * @date 2021/5/16 19:35
 */
class PhotoSelector(private val photoManager: PhotoManager) {

    private val selectedPhotoList = ArrayList<SelectedPhoto>()

    private val tempList = LinkedList<SelectedPhoto>()

    private var selectedChangeListener: OnSelectedChangeListener? = null

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

    fun onSelectedChanged(listener: OnSelectedChangeListener) {
        this.selectedChangeListener = listener
    }

    fun onPhotoChecked(photo: Photo) {
        tempList.clear()
        for (selected in selectedPhotoList) {
            if (selected.photo == photo || selected.photo.uri == photo.uri) {
                tempList.add(selected)
            }
        }
        if (tempList.isEmpty()) {
            selectedPhotoList.add(SelectedPhoto(photoManager.indexOf(photo), photo))
        } else {
            selectedPhotoList.removeAll(tempList)
        }
        this.selectedChangeListener?.onSelectedChange()
    }

    fun interface OnSelectedChangeListener {
        fun onSelectedChange()
    }

    data class SelectedPhoto(
        val position: Int,
        val photo: Photo
    )

}