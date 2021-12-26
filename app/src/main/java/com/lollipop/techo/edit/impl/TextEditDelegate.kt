package com.lollipop.techo.edit.impl

import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.bind
import com.lollipop.techo.data.BaseTechoItem
import com.lollipop.techo.data.BaseTextItem
import com.lollipop.techo.databinding.PanelTextEditBinding
import com.lollipop.techo.edit.EditDelegate

/**
 * @author lollipop
 * @date 2021/12/23 22:36
 */
class TextEditDelegate: EditDelegate() {

    private var binding: PanelTextEditBinding? = null

    override fun isSupport(info: BaseTechoItem): Boolean {
        return info is BaseTextItem
    }

    override fun onCreateView(container: ViewGroup): View {
        binding?.let {
            return it.root
        }
        val newBinding: PanelTextEditBinding = container.bind(false)
        binding = newBinding
        return newBinding.root
    }
}