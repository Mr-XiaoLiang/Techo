package com.lollipop.lqrdemo.creator.subpage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.databinding.FragmentQrCornerBinding

/**
 * 圆角设置的页面，它和背景设置其实是一起的，但是业务上，放不到同一个页面里面
 */
class QrCornerFragment : QrBaseSubpageFragment() {

    private val binding: FragmentQrCornerBinding by lazyBind()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

}