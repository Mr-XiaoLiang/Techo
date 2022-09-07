package com.lollipop.techo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByMargin
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.R
import com.lollipop.techo.databinding.FragmentRichTextOptionBinding

class RichTextOptionFragment : PageFragment() {

    private val binding: FragmentRichTextOptionBinding by lazyBind()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButton.fixInsetsByMargin(WindowInsetsHelper.Edge.HEADER)
        bindBackButton(binding.backButton)
        initView()
    }

    private fun initView() {
        binding.panelMenuBar.setOnItemSelectedListener {
            onPanelSelected(it.itemId)
        }
    }

    private fun onPanelSelected(itemId: Int): Boolean {
        with(binding) {
            selectorPanel.isVisible = false
            layerPanel.isVisible = false
            richOptionPanel.isVisible = false
            palettePanel.isVisible = false
            textSizePanel.isVisible = false
        }
        when (itemId) {
            R.id.menuSelector -> {
                binding.selectorPanel.isVisible = true
            }
            R.id.menuLayer -> {
                binding.layerPanel.isVisible = true
            }
            R.id.menuFontSize -> {
                binding.textSizePanel.isVisible = true
            }
            R.id.menuRichStyle -> {
                binding.richOptionPanel.isVisible = true
            }
            R.id.menuPalette -> {
                binding.palettePanel.isVisible = true
            }
            else -> {
                return false
            }
        }
        return true
    }

}