package com.lollipop.techo.edit.impl

import android.view.View
import android.view.ViewGroup
import com.lollipop.base.request.startPermissionFlow
import com.lollipop.base.util.bind
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import com.lollipop.gallery.PhotoManager
import com.lollipop.techo.data.BaseTechoItem
import com.lollipop.techo.data.PhotoItem
import com.lollipop.techo.databinding.PanelPhotoSelectBinding
import com.lollipop.techo.edit.EditDelegate

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

    }

    override fun onOpen(info: BaseTechoItem) {
        super.onOpen(info)

    }

    private fun loadPhotos() {
        val activity = context
        if (activity == null) {
            onPermissionDenied()
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
                        val photoSize = photoManager.size
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

}