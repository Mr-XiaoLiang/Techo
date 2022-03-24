package com.lollipop.web

import android.view.View
import com.lollipop.web.bridge.Bridge
import com.lollipop.web.bridge.BridgeRoot
import com.lollipop.web.completion.UrlCompletion
import com.lollipop.web.completion.UrlCompletionResult
import com.lollipop.web.completion.impl.EmptyCompletion
import com.lollipop.web.listener.ProgressListener
import com.lollipop.web.listener.TitleListener
import com.lollipop.web.search.SearchEngine
import com.lollipop.web.search.SearchEngineCallback
import com.lollipop.web.search.impl.Bing

/**
 * 放弃继承和包装，采用组合组件的形式来提供Web的支持能力
 */
class WebHelper(val iWeb: IWeb) : UrlCompletionResult, SearchEngineCallback {

    companion object {

        private val globeBridgeRoot = ArrayList<Class<out BridgeRoot>>()
        private val globeBridge = HashMap<String, ArrayList<Class<out Bridge>>>()

        private var globeUrlCompletion: Class<out UrlCompletion> = EmptyCompletion::class.java

        private var globeSearchEngine: Class<out SearchEngine> = Bing::class.java

        fun setGlobeUrlCompletion(clazz: Class<out UrlCompletion>) {
            globeUrlCompletion = clazz
        }

        fun setGlobeSearchEngine(clazz: Class<out SearchEngine>) {
            globeSearchEngine = clazz
        }

        fun bind(host: WebHost, view: View): WebHelper {
            return WebHelper(IWebFactory.create(host, view))
        }

        fun register(clazz: Class<out BridgeRoot>) {
            if (globeBridgeRoot.contains(clazz)) {
                return
            }
            globeBridgeRoot.add(clazz)
        }

        fun register(rootName: String, clazz: Class<out Bridge>) {
            val bridgeList = globeBridge[rootName] ?: ArrayList()
            if (bridgeList.contains(clazz)) {
                return
            }
            bridgeList.add(clazz)
            globeBridge[rootName] = bridgeList
        }

        fun unregister(clazz: Class<out BridgeRoot>) {
            globeBridgeRoot.remove(clazz)
        }

        fun unregister(rootName: String, clazz: Class<out Bridge>) {
            val bridgeList = globeBridge[rootName] ?: return
            bridgeList.remove(clazz)
        }

    }

    private var isInit = false

    private var urlCompletion: UrlCompletion? = null

    private var searchEngine: SearchEngine? = null

    private val defaultCompletion by lazy { EmptyCompletion() }

    private val defaultSearchEngine by lazy { Bing() }

    val canRegisterBridgeRoot: Boolean
        get() {
            return !isInit
        }

    private val bridgeRootList = ArrayList<BridgeRoot>()

    fun setUrlCompletion(completion: UrlCompletion): WebHelper {
        this.urlCompletion = completion
        return this
    }

    fun addBridgeRoot(bridge: BridgeRoot): WebHelper {
        iWeb.addBridgeRoot(bridge)
        return this
    }

    fun addBridge(rootName: String, bridge: Bridge): WebHelper {
        this.bridgeRootList.forEach {
            if (it.name == rootName) {
                it.addBridge(bridge)
                return this
            }
        }
        return this
    }

    fun init(): WebHelper {
        if (isInit) {
            return this
        }
        isInit = true
        globeBridgeRoot.forEach {
            addBridgeRoot(it.newInstance())
        }
        globeBridge.forEach { entry ->
            val key = entry.key
            this.bridgeRootList.find { it.name == key }?.let { root ->
                entry.value.forEach { bridge ->
                    root.addBridge(bridge.newInstance())
                }
            }
        }
        if (urlCompletion == null) {
            urlCompletion = globeUrlCompletion.newInstance()
        }
        if (searchEngine != null) {
            searchEngine = globeSearchEngine.newInstance()
        }
        return this
    }

    fun loadUrl(url: String): WebHelper {
        init()
        getCompletion().complement(url, this)
        return this
    }

    private fun getCompletion(): UrlCompletion {
        return urlCompletion ?: defaultCompletion
    }

    private fun getSearchEngine(): SearchEngine {
        return searchEngine ?: defaultSearchEngine
    }

    fun onProgressChanged(listener: ProgressListener): WebHelper {
        iWeb.setProgressListener(listener)
        return this
    }

    fun onTitleChanged(listener: TitleListener): WebHelper {
        iWeb.setTitleListener(listener)
        return this
    }

    override fun onLoadUrl(url: String) {
        iWeb.load(url)
    }

    override fun onSearch(keyword: String) {
        getSearchEngine().getSearchUrl(keyword, this)
    }

    override fun onSearchEngineResult(url: String) {
        onLoadUrl(url)
    }

    override fun onSearchRelevantResult(values: List<String>) {
        // TODO("Not yet implemented")
    }

}