package com.lollipop.browser.web

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.lollipop.base.ui.BaseFragment
import com.lollipop.base.util.lazyBind
import com.lollipop.browser.databinding.FragmentWebPageBinding
import com.lollipop.web.IWeb
import com.lollipop.web.WebHelper
import com.lollipop.web.WebHost
import com.lollipop.web.listener.ProgressListener
import com.lollipop.web.listener.TitleListener

class WebPageFragment : BaseFragment(),
    WebHost,
    BrowserMainPanel.Callback,
    TitleListener,
    ProgressListener {

    companion object {

        private const val ARG_PAGE_ID = "ARG_PAGE_ID"

        fun putArguments(arguments: Bundle, pageId: String) {
            arguments.putString(ARG_PAGE_ID, pageId)
        }

        private fun getPageId(arguments: Bundle?): String {
            return arguments?.getString(ARG_PAGE_ID) ?: ""
        }
    }

    private val binding: FragmentWebPageBinding by lazyBind()

    private var loadStatusCallback: LoadStatusCallback? = null

    private var webHelper: WebHelper? = null

    private var browserMainPanel: BrowserMainPanel? = null

    private val pageId: String
        get() {
            return getPageId(arguments)
        }

    override val hostLifecycle: Lifecycle
        get() {
            return this.lifecycle
        }

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
        val newHelper = WebHelper.bind(this, binding.webView)
        initWeb(newHelper)
        webHelper = newHelper
        browserMainPanel = BrowserMainPanel.bind(binding.pageRoot)
    }

    private fun initWeb(helper: WebHelper) {
        helper.onTitleChanged(this)
        // TODO
    }

    fun load(url: String) {
        // TODO
    }

    override fun onTitleChanged(iWeb: IWeb, title: String) {
        TODO("Not yet implemented")
    }

    override fun onIconChanged(iWeb: IWeb, icon: Bitmap?) {
        TODO("Not yet implemented")
    }

    override fun onProgressChanged(iWeb: IWeb, progress: Int) {
        if (iWeb.host === this) {
            loadStatusCallback?.onLoadProgressChanged(pageId = pageId, progress = progress * 0.01F)
        }
    }

    interface LoadStatusCallback {

        fun onLoadStart(pageId: String, url: String)

        /**
         * @param progress [0F, 1F]
         */
        fun onLoadProgressChanged(pageId: String, progress: Float)

        fun onLoadFinished(pageId: String, title: String, url: String)

    }


}