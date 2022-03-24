package com.lollipop.web

class IWebConfig {

    companion object {
        const val TEXT_ENCODING_UTF8 = "utf-8"
        const val FONT_SIZE_DEFAULT = 16
        const val FONT_SIZE_MIN = 8
        val FONT_SIZE_RANGE = 1..72
        const val TEXT_ZOOM = 100
        const val FONT_FAMILY_CURSIVE = "cursive"
        const val FONT_FAMILY_FANTASY = "fantasy"
        const val FONT_FAMILY_MONOSPACE = "monospace"
        const val FONT_FAMILY_SANS_SERIF = "sans-serif"
    }

    /**
     * 如果访问的页面中要与Javascript交互，则webView必须设置支持Javascript
     */
    var javaScriptEnabled = false

    /**
     * 支持插件
     */
    var pluginsEnabled = false

    /**
     * 将图片调整到适合webView的大小
     */
    var useWideViewPort = true

    /**
     * 缩放至屏幕的大小
     */
    var loadWithOverviewMode = true

    /**
     * 支持缩放
     */
    var supportZoom = true

    /**
     * 设置内置的缩放控件
     */
    var builtInZoomControls = true

    /**
     * 显示原生的缩放控件
     */
    var displayZoomControls = false

    /**
     * 缓存模式
     */
    var cacheMode: CacheMode = CacheMode.DEFAULT

    /**
     * 允许访问文件
     */
    var allowFileAccess = false

    /**
     * javaScript可以打开新的窗口
     */
    var javaScriptCanOpenWindowsAutomatically = false

    /**
     * 自动加载图片
     */
    var loadsImagesAutomatically = true

    /**
     * 默认的字符集编码
     */
    var defaultTextEncodingName = TEXT_ENCODING_UTF8

    /**
     * 强制暗色模式
     */
    var forceDark: ForceDark = ForceDark.AUTO

    /**
     * 允许访问contentProvider提供的内容
     */
    var allowContentAccess = true

    /**
     * 禁止加载所有网络图片
     */
    var blockNetworkImage = false

    /**
     * 禁止所有网络加载（包括图片
     */
    var blockNetworkLoads = false

    /**
     * Sets the cursive font family name. The default is "cursive".
     */
    var cursiveFontFamily = FONT_FAMILY_CURSIVE

    /**
     * Sets the fantasy font family name. The default is "fantasy".
     */
    var fantasyFontFamily = FONT_FAMILY_FANTASY

    /**
     * Sets the fixed font family name. The default is "monospace".
     */
    var fixedFontFamily = FONT_FAMILY_MONOSPACE

    /**
     * Sets the sans-serif font family name. The default is "sans-serif".
     */
    var sansSerifFontFamily = FONT_FAMILY_SANS_SERIF

    /**
     * Sets the serif font family name. The default is "sans-serif".
     */
    var serifFontFamily = FONT_FAMILY_SANS_SERIF

    /**
     * Sets the standard font family name. The default is "sans-serif".
     */
    var standardFontFamily = FONT_FAMILY_SANS_SERIF

    /**
     * 启用数据库功能
     */
    var databaseEnabled = true

    /**
     * 设置默认的固定字体大小。默认值为16。
     * 取值范围为1～72
     */
    var defaultFixedFontSize = FONT_SIZE_DEFAULT
        set(value) {
            if (value in FONT_SIZE_RANGE) {
                field = value
            }
        }

    /**
     * 设置默认的字体大小。默认值为16。
     * 取值范围为1～72
     */
    var defaultFontSize = FONT_SIZE_DEFAULT
        set(value) {
            if (value in FONT_SIZE_RANGE) {
                field = value
            }
        }

    /**
     * 最小文字大小，默认为8
     * 取值范围为1～72
     */
    var minimumFontSize = FONT_SIZE_MIN
        set(value) {
            if (value in FONT_SIZE_RANGE) {
                field = value
            }
        }

    /**
     * 设置最小逻辑字体大小。默认值为8。
     */
    var minimumLogicalFontSize = FONT_SIZE_MIN
        set(value) {
            if (value in FONT_SIZE_RANGE) {
                field = value
            }
        }

    /**
     * 设置是否启用DOM存储API。默认值为false。
     */
    var domStorageEnabled = false

    /**
     * 允许获取位置信息
     * 它必须在设置IWeb.setGeolocationPermissionsListener
     * 之后才能生效
     */
    var geolocationEnabled = true

    /**
     * 设置WebView是否需要用户手势来播放媒体。默认值为true。
     */
    var mediaPlaybackRequiresUserGesture = true

    /**
     * 内容混合模式
     */
    var mixedContentMode: MixedContentMode = MixedContentMode.NEVER_ALLOW

    /**
     * 是否将WebView的元素设置为具有焦点，需要使用
     * WebView.requestFocus(int, android.graphics.Rect)
     */
    var needInitialFocus = true

    /**
     * 设置是否支持离屏渲染
     * 它生效的前提是被附加在一个window上
     * 但是请注意，它会增加很大的消耗，因此请保证以下两点：
     * WebView 大小不应大于设备屏幕大小。
     * 将此模式的使用限制为少量 WebView。 将它用于可见的 WebView 和即将被动画化为可见的 WebView。
     */
    var offscreenPreRaster = false

    /**
     * 是否启用安全浏览
     * 安全浏览允许 WebView 通过验证链接来防止恶意软件和网络钓鱼攻击。
     */
    var safeBrowsingEnabled = true

    /**
     * 设置 WebView 是否支持多窗口。
     * 如果设置为 true，则 WebChromeClient.onCreateWindow 必须由主机应用程序实现。
     * 默认值为false。
     */
    var supportMultipleWindows = false

    /**
     * 以百分比设置页面的文本缩放。 默认值为 100。
     */
    var textZoom = TEXT_ZOOM

    /**
     * 内容混合模式
     * 配置安全源尝试从不安全源加载资源时WebView的行为。
     */
    enum class MixedContentMode {
        /**
         * 始终拒绝不安全源
         * 推荐
         */
        NEVER_ALLOW,

        /**
         * 始终同意不安全源
         * 强烈不建议
         */
        ALWAYS_ALLOW,

        /**
         * 混合模式
         */
        COMPATIBILITY_MODE
    }

    /**
     * 强制深色模式
     */
    enum class ForceDark {
        /**
         * 关闭强制黑色模式
         */
        OFF,

        /**
         * 自动强制黑色模式
         */
        AUTO,

        /**
         * 开启强制黑色模式
         */
        ON
    }

    /**
     * 缓存模式
     */
    enum class CacheMode {
        /**
         * （默认）根据cache-control决定是否从网络上取数据。
         */
        DEFAULT,

        /**
         * 只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据
         */
        CACHE_ELSE_NETWORK,

        /**
         * 不使用缓存，只从网络获取数据.
         */
        NO_CACHE,

        /**
         * 不使用网络，只读取本地缓存数据
         */
        CACHE_ONLY
    }

}