package com.lollipop.techo.edit.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.gallery.Photo
import com.lollipop.gallery.PhotoManager
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.ItemPhotoEditBinding
import com.lollipop.techo.databinding.PanelPhotoSelectBinding
import com.lollipop.techo.edit.base.TopEditDelegate
import com.lollipop.techo.util.load
import com.lollipop.techo.util.permission.PermissionInfo

/**
 * @author lollipop
 * @date 2021/12/23 22:36
 */
class PhotoEditDelegate : TopEditDelegate<TechoItem.Photo>() {

    private var binding: PanelPhotoSelectBinding? = null

    private val photoManager = PhotoManager.create()

    private val selectedPhotoList = SparseArray<Photo>()

    private val adapter by lazy {
        PhotoAdapter(photoManager, ::onPhotoClick, ::getSelectedIndex)
    }

    override val permissions: Array<PermissionInfo> = arrayOf(
        PermissionInfo(
            permission = PhotoManager.getPermissions(),
            rationale = R.string.permission_rationale_read_external_storage,
            anyOne = true
        )
    )

    override val contentGroup: View?
        get() {
            return binding?.backgroundView
        }
    override val backgroundView: View?
        get() {
            return binding?.photoSelectCard
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
            it.photoSelectGroup.adapter = adapter
        }
    }

    override fun onOpen(info: TechoItem.Photo) {
        super.onOpen(info)
        val activity = context
        if (activity == null) {
            onPhotoLoadError()
            return
        }
        loadPhotos(activity)
    }

    override fun onAnimationUpdate(progress: Float) {
        super.onAnimationUpdate(progress)
        binding?.apply {
            animationAlpha(progress, doneBtn)
        }
    }

    private fun onPhotoClick(check: Boolean, position: Int) {
        if (check) {
            if (selectedPhotoList.indexOfKey(position) >= 0) {
                selectedPhotoList.remove(position)
            } else {
                selectedPhotoList.put(position, photoManager[position])
            }
            adapter.notifyItemChanged(position)
        } else {
            showPhotoDetail(position)
        }
    }

    private fun getSelectedIndex(position: Int): Int {
        val indexOfKey = selectedPhotoList.indexOfKey(position)
        if (indexOfKey >= 0) {
            return indexOfKey + 1
        }
        return -1
    }

    private fun showPhotoDetail(position: Int) {
        // TODO()
        // 打开图片详情页
    }

    private fun loadPhotos(activity: Activity) {
        onPhotoLoadStart()
        if (!photoManager.isMediaStoreChanged) {
            onPhotoLoaded()
            return
        }
        val launcher = findLauncher(PhotoManager.getPermissions())
        if (launcher == null) {
            onPermissionDenied()
            return
        }
        launcher.request { result ->
            if (result.isGranted()) {
                load(activity)
            } else {
                onPermissionDenied()
            }
        }
    }

    private fun load(activity: Activity) {
        doAsync({
            onUI {
                onPhotoLoadError()
            }
        }) {
            photoManager.refresh(activity)
            onUI {
                onPhotoLoaded()
            }
        }
    }

    private fun onPermissionDenied() {
        binding?.let { bd ->
            bd.photoSelectGroup.isVisible = false
            bd.statusPanel.isVisible = true
            bd.statusMsgView.setText(R.string.photo_permission_denied)
        }
    }

    private fun onPhotoLoadStart() {
        binding?.let { bd ->
            bd.photoSelectGroup.isVisible = true
            bd.statusPanel.isVisible = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onPhotoLoaded() {
        // 加载完成后顺序可能变化，所以清空
        selectedPhotoList.clear()
        adapter.notifyDataSetChanged()
    }

    private fun onPhotoLoadError() {
        binding?.let { bd ->
            bd.photoSelectGroup.isVisible = false
            bd.statusPanel.isVisible = true
            bd.statusMsgView.setText(R.string.photo_load_error)
        }
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