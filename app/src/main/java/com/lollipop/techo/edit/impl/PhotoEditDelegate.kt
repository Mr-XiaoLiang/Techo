package com.lollipop.techo.edit.impl

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.request.startPermissionFlow
import com.lollipop.base.util.bind
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onClick
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

    private val photoManager = PhotoManager.create()

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
        val activity = context
        if (activity == null) {
            onPhotoLoadError()
            return
        }
        if (PhotoManager.checkPermission(activity)) {
            loadPhotos(activity)
        } else {
            startPermissionFlow { flow ->
                if (flow == null) {
                    onPermissionDenied()
                } else {
                    flow.permissionIs(PhotoManager.READ_PERMISSION)
                        .request { result ->
                            if (result.isGranted(PhotoManager.READ_PERMISSION)) {
                                loadPhotos(activity)
                            } else {
                                onPermissionDenied()
                            }
                        }
                }
            }
        }
    }

    private fun loadPhotos(activity: Activity) {
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

    private class PhotoAdapter(
        private val data: List<Photo>,
        private val photoClickListener: PhotoClickListener,
        private val selectedIndexProvider: PhotoSelectedStatusProvider
    ) : RecyclerView.Adapter<ItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder.create(parent, photoClickListener)
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            holder.bind(data[position], selectedIndexProvider.getSelectedIndex(position))
        }

        override fun getItemCount(): Int {
            return data.size
        }

    }

    private class ItemHolder(
        private val binding: ItemPhotoEditBinding,
        private val onClickListener: PhotoClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun create(parent: ViewGroup, onClickListener: PhotoClickListener): ItemHolder {
                return ItemHolder(parent.bind(), onClickListener)
            }
        }

        init {
            binding.photoView.onClick {
                onPhotoClick()
            }
            binding.photoCheckView.onClick {
                onCheckboxClick()
            }
        }

        private fun onPhotoClick() {
            onClickListener.onPhotoClick(false, adapterPosition)
        }

        private fun onCheckboxClick() {
            onClickListener.onPhotoClick(true, adapterPosition)
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

    private fun interface PhotoSelectedStatusProvider {
        fun getSelectedIndex(position: Int): Int
    }

    private fun interface PhotoClickListener {
        fun onPhotoClick(check: Boolean, position: Int)
    }

}