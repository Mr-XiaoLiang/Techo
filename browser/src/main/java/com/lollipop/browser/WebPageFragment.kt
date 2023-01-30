package com.lollipop.browser

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.base.ui.BaseFragment
import com.lollipop.base.util.lazyBind
import com.lollipop.browser.databinding.FragmentWebPageBinding

class WebPageFragment : BaseFragment() {

    private val binding: FragmentWebPageBinding by lazyBind()

    private var loadStatusCallback: LoadStatusCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadStatusCallback = check(context)
    }

    override fun onDetach() {
        super.onDetach()
        loadStatusCallback = null
    }

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

    fun load(url: String) {
        // TODO
    }

    interface LoadStatusCallback {

        fun onLoadStart(url: String)

        fun onLoadProgressChanged(progress: Float)

        fun onLoadFinished(title: String, url: String)

    }

}