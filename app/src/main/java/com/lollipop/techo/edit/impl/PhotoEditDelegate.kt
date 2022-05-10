package com.lollipop.techo.edit.impl

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lollipop.base.request.startPermissionFlow
import com.lollipop.base.util.bind
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import com.lollipop.gallery.Photo
import com.lollipop.gallery.PhotoManager
import com.lollipop.techo.data.BaseTechoItem
import com.lollipop.techo.data.PhotoItem
import com.lollipop.techo.databinding.ItemPhotoEditBinding
import com.lollipop.techo.databinding.PanelPhotoSelectBinding
import com.lollipop.techo.edit.EditDelegate
import com.lollipop.techo.util.load

/**
 * @author lollipop
 * @date 2021/12/23 22:36
 */
class PhotoEditDelegate : EditDelegate() {

    private var binding: PanelPhotoSelectBinding? = null

    private val photoManager = PhotoManager()

    override fun isSupport(info: BaseTechoItem): Boolean {
        return info is PhotoItem
    }

    override fun onCreateView(container: ViewGroup): View {
        binding?.let {
            return it.root
        }
        val newBinding: PanelPhotoSelectBinding = container.bind(false)
        binding = newBinding
        return newBinding.root
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        binding?.let {
            it.photoSelectGroup.layoutManager = GridLayoutManager(view.context, 4)
        }
    }

    override fun onOpen(info: BaseTechoItem) {
        super.onOpen(info)
        loadPhotos()
    }

    private fun loadPhotos() {
        val activity = context
        if (activity == null) {
            onPhotoLoadError()
            return
        }
        onPhotoLoadStart()
        if (!photoManager.isMediaStoreChanged) {
            onPhotoLoaded()
            return
        }
        activity.startPermissionFlow()
            .permissionIs(PhotoManager.READ_PERMISSION)
            .request {
                doAsync({
                    onUI {
                        onPhotoLoadError()
                    }
                }) {
                    context?.let { c ->
                        photoManager.refresh(c)
//                        val photoSize = photoManager.size
                        onUI {
                            onPhotoLoaded()
                        }
                    }
                }
            }
    }

    private fun onPermissionDenied() {
        // TODO
    }

    private fun onPhotoLoadStart() {
        // TODO
    }

    private fun onPhotoLoaded() {
        // TODO
    }

    private fun onPhotoLoadError() {
        // TODO
    }

//    private class PhotoAdapter:

    private class ItemHolder(
        private val binding: ItemPhotoEditBinding,
        private val onClickListener: (Boolean, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun create(parent: ViewGroup, onClickListener: (Boolean, Int) -> Unit): ItemHolder {
                return ItemHolder(parent.bind(), onClickListener)
            }
        }

        init {
            binding.photoView.setOnClickListener {
                onPhotoClick()
            }
            binding.photoCheckView.setOnClickListener {
                onCheckboxClick()
            }
        }

        private fun onPhotoClick() {
            onClickListener(false, adapterPosition)
        }

        private fun onCheckboxClick() {
            onClickListener(true, adapterPosition)
        }

        fun bind(info: Photo, selectedIndex: Int) {
            binding.photoView.load(info.uri)
            val isChecked = selectedIndex > 0
            binding.photoCheckView.isChecked = isChecked
            binding.photoNumberView.text = if (isChecked) {
                "$selectedIndex"
            } else {
                ""
            }
        }


    }

}