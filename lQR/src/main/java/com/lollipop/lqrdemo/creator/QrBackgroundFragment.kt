package com.lollipop.lqrdemo.creator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.databinding.FragmentQrEditBackgroundBinding

/**
 * 背景的设置
 */
class QrBackgroundFragment : BaseFragment() {

    private val binding: FragmentQrEditBackgroundBinding by lazyBind()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}