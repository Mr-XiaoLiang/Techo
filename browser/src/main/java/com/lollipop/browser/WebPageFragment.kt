package com.lollipop.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lollipop.base.util.lazyBind
import com.lollipop.browser.databinding.FragmentWebPageBinding

class WebPageFragment : Fragment() {

    private val binding: FragmentWebPageBinding by lazyBind()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

}