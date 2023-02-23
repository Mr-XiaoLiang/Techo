package com.lollipop.web

import android.graphics.Bitmap
import android.view.View
import com.lollipop.web.bridge.Bridge
import com.lollipop.web.bridge.BridgeAlias
import com.lollipop.web.bridge.BridgeRoot
import com.lollipop.web.completion.UrlCompletion
import com.lollipop.web.completion.UrlCompletionResult
import com.lollipop.web.completion.impl.EmptyCompletion
import com.lollipop.web.listener.GeolocationPermissionsListener
import com.lollipop.web.listener.ProgressListener
import com.lollipop.web.listener.TitleListener
import com.lollipop.web.listener.WindowListener
import com.lollipop.web.search.SearchEngine
import com.lollipop.web.search.SearchEngineCallback
import com.lollipop.web.search.SearchSuggestion
import com.lollipop.web.search.impl.Bing

/**
 * 放弃继承和包装，采用组合组件的形式来提供Web的支持能力
 */
class WebHelper(
    val iWeb: IWeb,
    private val searchEngineCallback: SearchEngineCallback?
) : UrlCompletionResult, SearchEngineCallback {

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

        fun bind(
            host: WebHost,
            view: View,
            searchEngineCallback: SearchEngineCallback? = null
        ): WebHelper {
            return WebHelper(IWebFactory.create(host, view), searchEngineCallback)
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

    private val defaultSearchEngine by lazy { Bing(iWeb.host.hostLifecycle) }

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
        if (canRegisterBridgeRoot) {
            bridgeRootList.add(bridge)
        }
        return this
    }

    fun addBridge(rootName: String, bridge: Bridge, vararg alias: String): WebHelper {
        this.bridgeRootList.forEach {
            if (it.name == rootName) {
                it.addBridge(bridge)
                alias.forEach { name ->
                    it.addBridge(BridgeAlias(name, bridge))
                }
            }
        }
        return this
    }

    fun init(): WebHelper {
        if (isInit) {
            return this
        }
        isInit = true
        initGlobeBridge()
        this.bridgeRootList.forEach {
            iWeb.addBridgeRoot(it)
        }
        if (urlCompletion == null) {
            urlCompletion = globeUrlCompletion.newInstance()
        }
        if (searchEngine != null) {
            searchEngine = globeSearchEngine.newInstance()
        }
        return this
    }

    private fun initGlobeBridge() {
        globeBridgeRoot.forEach {
            addBridgeRoot(it.newInstance())
        }
        globeBridge.forEach { entry ->
            val key = entry.key
            this.bridgeRootList.forEach { root ->
                if (root.name == key) {
                    entry.value.forEach { bridge ->
                        root.addBridge(bridge.newInstance())
                    }
                }
            }
        }
    }

    fun loadUrl(url: String, now: Boolean = false): WebHelper {
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

    fun onTitleChanged(run: SimpleTitleListener.() -> Unit): WebHelper {
        return onTitleChanged(SimpleTitleListener().apply { run() })
    }

    fun setGeolocationPermissionsListener(listener: GeolocationPermissionsListener): WebHelper {
        iWeb.setGeolocationPermissionsListener(listener)
        return this
    }

    fun setWindowListener(listener: WindowListener): WebHelper {
        iWeb.setWindowListener(listener)
        return this
    }

    fun setConfig(config: IWebConfig): WebHelper {
        iWeb.setConfig(config)
        return this
    }

    override fun onLoadUrl(url: String) {
        iWeb.load(url)
    }

    override fun onSearch(keyword: String) {
        getSearchEngine().search(keyword, this)
    }

    class SimpleTitleListener : TitleListener {

        private var onTitleChangedCallback: ((iWeb: IWeb, title: String) -> Unit)? = null
        private var onIconChangedCallback: ((iWeb: IWeb, icon: Bitmap?) -> Unit)? = null

        override fun onTitleChanged(iWeb: IWeb, title: String) {
            onTitleChangedCallback?.invoke(iWeb, title)
        }

        override fun onIconChanged(iWeb: IWeb, icon: Bitmap?) {
            onIconChangedCallback?.invoke(iWeb, icon)
        }

        fun onTitleChanged(callback: ((iWeb: IWeb, title: String) -> Unit)?) {
            this.onTitleChangedCallback = callback
        }

        fun onIconChanged(callback: ((iWeb: IWeb, icon: Bitmap?) -> Unit)?) {
            this.onIconChangedCallback = callback
        }

    }

    override fun onSearchRelevantResult(values: List<SearchSuggestion>) {
        searchEngineCallback?.onSearchRelevantResult(values)
    }

}