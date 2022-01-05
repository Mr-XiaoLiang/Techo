package com.lollipop.techo.edit.impl

import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.bind
import com.lollipop.techo.data.BaseTechoItem
import com.lollipop.techo.data.PhotoItem
import com.lollipop.techo.databinding.PanelPhotoSelectBinding
import com.lollipop.techo.edit.EditDelegate

/**
 * @author lollipop
 * @date 2021/12/23 22:36
 */
class PhotoEditDelegate: EditDelegate() {

    private var binding: PanelPhotoSelectBinding? = null

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
}