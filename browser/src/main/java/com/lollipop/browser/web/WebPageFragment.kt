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
import com.lollipop.browser.main.MainPageDelegate
import com.lollipop.web.IWeb
import com.lollipop.web.WebHelper
import com.lollipop.web.WebHost
import com.lollipop.web.listener.ProgressListener
import com.lollipop.web.listener.TitleListener
import com.lollipop.web.search.SearchEngineCallback
import com.lollipop.web.search.SearchSuggestion

class WebPageFragment : BaseFragment(),
    WebHost,
    BrowserMainPanel.Callback,
    TitleListener,
    ProgressListener,
    SearchEngineCallback {

    companion object {

        private const val ARG_PAGE_ID = "ARG_PAGE_ID"
        private const val ARG_URL = "ARG_URL"
        fun putArguments(arguments: Bundle, pageId: String, url: String) {
            arguments.putString(ARG_PAGE_ID, pageId)
            arguments.putString(ARG_URL, url)
        }

        private fun getPageId(arguments: Bundle?): String {
            return arguments?.getString(ARG_PAGE_ID) ?: ""
        }

        private fun getUrl(arguments: Bundle?): String {
            return arguments?.getString(ARG_URL) ?: ""
        }

        private fun clearArgumentsUrl(arguments: Bundle?) {
            arguments?.remove(ARG_URL)
        }

    }

    private val binding: FragmentWebPageBinding by lazyBind()

    private var callback: Callback? = null

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

    private var mainPageDelegate: MainPageDelegate? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = check(context)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
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
        helper.onProgressChanged(this)
    }

    override fun onStart() {
        super.onStart()
        val presetUrl = getUrl(arguments)
        if (presetUrl.isEmpty()) {
            if (mainPageDelegate == null) {
                mainPageDelegate = MainPageDelegate.inflate(binding.pageContainerView)
            }
        } else {
            load(presetUrl)
            clearArgumentsUrl(arguments)
        }
    }

    fun load(url: String) {
        webHelper?.loadUrl(url)
        mainPageDelegate?.destroy()
        mainPageDelegate = null
    }

    override fun onResume() {
        super.onResume()
        mainPageDelegate?.resume()
    }

    override fun onTitleChanged(iWeb: IWeb, title: String) {
        if (iWeb.host === this) {
            callback?.onTitleChanged(pageId = pageId, title = title)
        }
    }

    override fun onIconChanged(iWeb: IWeb, icon: Bitmap?) {
        if (iWeb.host === this) {
            callback?.onIconChanged(pageId = pageId, icon = icon)
        }
    }

    override fun onProgressChanged(iWeb: IWeb, progress: Int) {
        if (iWeb.host === this) {
            callback?.onLoadProgressChanged(pageId = pageId, progress = progress * 0.01F)
        }
    }

    override fun onSearchRelevantResult(values: List<SearchSuggestion>) {
        callback?.onSearchRelevantResult(pageId = pageId, values = values)
    }

    interface Callback {

        fun onLoadStart(pageId: String, url: String)

        /**
         * @param progress [0F, 1F]
         */
        fun onLoadProgressChanged(pageId: String, progress: Float)

        fun onTitleChanged(pageId: String, title: String)

        fun onIconChanged(pageId: String, icon: Bitmap?)

        fun onSearchRelevantResult(pageId: String, values: List<SearchSuggestion>)
    }


}