package com.lollipop.techo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.base.ui.BaseFragment
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.FragmentRichTextOptionBinding

class RichTextOptionFragment : BaseFragment() {

    private val binding: FragmentRichTextOptionBinding by lazyBind()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

}