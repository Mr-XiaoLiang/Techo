package com.lollipop.lqrdemo.preview.renderer

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import com.lollipop.lqrdemo.databinding.BarcodePreviewWebBinding
import com.lollipop.lqrdemo.preview.BarcodePreviewRenderer
import com.lollipop.pigment.Pigment
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.qr.comm.BarcodeWrapper

class WebViewRenderer : BarcodePreviewRenderer {

    private var binding: BarcodePreviewWebBinding? = null

    override fun getView(container: ViewGroup): View {
        val oldBinding = binding
        if (oldBinding != null) {
            return oldBinding.root
        }
        val newBinding = BarcodePreviewWebBinding.inflate(
            LayoutInflater.from(container.context),
            container,
            false
        )
        binding = newBinding
        initView(newBinding)
        return newBinding.root
    }

    override fun render(barcode: BarcodeWrapper) {
        val info = barcode.info
        if (info !is BarcodeInfo.Url) {
            return
        }
        val url = info.url
        binding?.apply {
            urlView.text = url
            if (webView.canGoBack()) {
                webView.clearHistory()
            }
            webView.loadUrl(url)
        }
    }

    override fun onDecorationChanged(pigment: Pigment) {
        binding?.apply {
            root.setBackgroundColor(pigment.backgroundColor)
            urlView.setTextColor(pigment.onBackgroundTitle)
            progressBar.setIndicatorColor(pigment.secondaryColor)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView(b: BarcodePreviewWebBinding) {
        val webView = b.webView
        val progressBar = b.progressBar
        progressBar.max = 100
        webView.webViewClient = WebClient(::onPageStarted, ::onPageFinished)
        webView.webChromeClient = ChromeClient(::onProgressChanged)
        webView.settings.apply {
            javaScriptEnabled = true
            displayZoomControls = false
            safeBrowsingEnabled = true
            setSupportZoom(true)
            useWideViewPort = true
            builtInZoomControls = true
        }
    }

    private fun onPageStarted(url: String) {
        binding?.apply {
            progressBar.isVisible = true
            progressBar.progress = 0
            urlView.text = url
        }
    }

    private fun onPageFinished(url: String) {
        binding?.apply {
            progressBar.isVisible = false
            urlView.text = url
        }
    }

    private fun onProgressChanged(progress: Int) {
        binding?.apply {
            progressBar.isVisible = true
            progressBar.progress = progress
        }
    }

    private class WebClient(
        private val onPageStarted: (String) -> Unit,
        private val onPageFinished: (String) -> Unit
    ) : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            url ?: return false
            view?.loadUrl(url)
            return true
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (view == null || request == null) {
                return super.shouldOverrideUrlLoading(view, request)
            }
            view.loadUrl(request.url.toString(), request.requestHeaders)
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            onPageStarted(url ?: "")
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onPageFinished(url ?: "")
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            handler?.proceed()
        }

    }

    private class ChromeClient(
        private val onProgressChanged: (Int) -> Unit
    ) : WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            onProgressChanged(newProgress)
        }
    }

}