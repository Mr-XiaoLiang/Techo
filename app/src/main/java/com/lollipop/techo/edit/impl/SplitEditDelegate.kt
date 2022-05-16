package com.lollipop.techo.edit.impl

import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.bind
import com.lollipop.techo.data.BaseTechoItem
import com.lollipop.techo.data.SplitItem
import com.lollipop.techo.databinding.PanelSplitSelectBinding
import com.lollipop.techo.edit.EditDelegate

/**
 * @author lollipop
 * @date 2021/12/23 22:36
 */
class SplitEditDelegate : EditDelegate() {

    private var binding: PanelSplitSelectBinding? = null

    override fun isSupport(info: BaseTechoItem): Boolean {
        return info is SplitItem
    }

    override fun onCreateView(container: ViewGroup): View {
        val newBinding: PanelSplitSelectBinding = container.bind(false)
        binding = newBinding
        return newBinding.root
    }
}