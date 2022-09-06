package com.lollipop.techo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByMargin
import com.lollipop.base.util.lazyBind
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
    }

}