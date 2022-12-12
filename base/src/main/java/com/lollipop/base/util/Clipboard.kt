package com.lollipop.base.util

import android.content.*
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle

/**
 * 剪切板的完整工具类
 * @author Lollipop
 * @date 2022/12/13
 */
object Clipboard {

    /**
     * 快捷拷贝一个文本
     * @param context 上下文
     * @param label 标签
     * @param value 拷贝内容
     */
    fun copy(context: Context, label: CharSequence = "Text", value: String) {
        try {
            copy(context, ClipData.newPlainText(label, value))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 快捷拷贝一个URI，它可以是路由地址，会通过内容解析器来分析内容类型
     * @param context 上下文
     * @param resolver 内容解析器
     * @param label 标签
     * @param value 拷贝内容
     */
    fun copy(
        context: Context,
        resolver: ContentResolver,
        label: CharSequence = "URI",
        value: Uri
    ) {
        try {
            copy(context, ClipData.newUri(resolver, label, value))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 快捷拷贝一个Intent
     * @param context 上下文
     * @param label 标签
     * @param value 拷贝内容
     */
    fun copy(context: Context, label: CharSequence = "Intent", value: Intent) {
        try {
            copy(context, ClipData.newIntent(label, value))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 快捷拷贝一个Html
     * @param context 上下文
     * @param label 标签
     * @param value 拷贝内容
     * @param html 拷贝的HTML原文
     */
    fun copy(context: Context, label: CharSequence = "Html", value: CharSequence, html: String) {
        try {
            copy(context, ClipData.newHtmlText(label, value, html))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 快捷拷贝一个原始URL
     * @param context 上下文
     * @param label 标签
     * @param value 拷贝内容
     */
    fun copy(context: Context, label: CharSequence = "URI", value: Uri) {
        try {
            copy(context, ClipData.newRawUri(label, value))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 打开拷贝编辑器，可以精细化控制拷贝内容
     * @param context 上下文
     */
    fun copy(context: Context): ClipBuilder {
        return ClipBuilder(context)
    }

    /**
     * 打开一个粘贴辅助工具
     * @param context 上下文
     */
    fun paste(context: Context): PasteHelper {
        return PasteHelper(context)
    }

    /**
     * 清空剪切板
     */
    fun clear(context: Context) {
        val clipboardService = getClipboardManager(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                clipboardService.clearPrimaryClip()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        try {
            clipboardService.setPrimaryClip(ClipData.newPlainText("", ""))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 增加剪切板的变更监听器
     */
    fun addPrimaryClipChangedListener(
        context: Context,
        listener: ClipboardManager.OnPrimaryClipChangedListener
    ) {
        getClipboardManager(context)?.addPrimaryClipChangedListener(listener)
    }

    /**
     * 移除剪切板的变更监听器
     */
    fun removePrimaryClipChangedListener(
        context: Context,
        listener: ClipboardManager.OnPrimaryClipChangedListener
    ) {
        getClipboardManager(context)?.removePrimaryClipChangedListener(listener)
    }

    private fun copy(context: Context, clipInfo: ClipData) {
        getClipboardManager(context)?.setPrimaryClip(clipInfo)
    }

    private fun getClipboardManager(context: Context): ClipboardManager? {
        val clipboardService = context.getSystemService(Context.CLIPBOARD_SERVICE)
        if (clipboardService is ClipboardManager) {
            return clipboardService
        }
        return null
    }

    /**
     * 拷贝编辑器
     * @param context 上下文
     */
    class ClipBuilder(private val context: Context) {

        private var label: CharSequence = ""
        private var mimeTypes = HashSet<String>()
        private val clipItemList = ArrayList<ClipData.Item>()
        private val extras = PersistableBundle()

        /**
         * 设置标签
         */
        fun label(value: CharSequence): ClipBuilder {
            this.label = value
            return this
        }

        private fun addItem(mimeType: String, item: ClipData.Item) {
            clipItemList.add(item)
            mimeTypes.add(mimeType)
        }

        /**
         * 设置文本类型的拷贝内容
         */
        fun copy(value: String): ClipBuilder {
            addItem(ClipDescription.MIMETYPE_TEXT_PLAIN, ClipData.Item(value))
            return this
        }

        /**
         * 复制自 ClipData.newUri(ContentResolver resolver, CharSequence label, Uri uri)
         */
        private fun getMimeTypes(resolver: ContentResolver, uri: Uri): Array<String?> {
            var mimeTypes: Array<String?>? = null
            if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
                val realType = resolver.getType(uri)
                mimeTypes = resolver.getStreamTypes(uri, "*/*")
                if (realType != null) {
                    if (mimeTypes == null) {
                        mimeTypes = arrayOf(realType)
                    } else if (!mimeTypes.contains(realType)) {
                        val tmp = arrayOfNulls<String>(mimeTypes.size + 1)
                        tmp[0] = realType
                        System.arraycopy(mimeTypes, 0, tmp, 1, mimeTypes.size)
                        mimeTypes = tmp
                    }
                }
            }
            if (mimeTypes == null) {
                mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_URILIST)
            }
            return mimeTypes
        }

        /**
         * 拷贝一个通过内容解析器解析的URI
         */
        fun copy(
            resolver: ContentResolver,
            value: Uri
        ): ClipBuilder {
            val types = getMimeTypes(resolver, value)
            var isEmpty = true
            types.forEach {
                if (it != null && it.isNotEmpty()) {
                    mimeTypes.add(it)
                    isEmpty = false
                }
            }
            if (isEmpty) {
                mimeTypes.add(ClipDescription.MIMETYPE_TEXT_URILIST)
            }
            clipItemList.add(ClipData.Item(value))
            return this
        }

        /**
         * 拷贝一个Intent
         */
        fun copy(value: Intent): ClipBuilder {
            addItem(ClipDescription.MIMETYPE_TEXT_INTENT, ClipData.Item(value))
            return this
        }

        /**
         * 拷贝一个HTML内容
         */
        fun copy(value: CharSequence, html: String): ClipBuilder {
            addItem(ClipDescription.MIMETYPE_TEXT_HTML, ClipData.Item(value, html))
            return this
        }

        /**
         * 拷贝一个原始的URI
         */
        fun copy(value: Uri): ClipBuilder {
            addItem(ClipDescription.MIMETYPE_TEXT_URILIST, ClipData.Item(value))
            return this
        }

        /**
         * 是否是隐秘内容
         * 如果设置为true，那么一般情况下，系统不会以明文显示在屏幕上
         * 比如这是一个密码，那么应该设置为true
         */
        fun isSensitive(value: Boolean): ClipBuilder {
            extra(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ClipDescription.EXTRA_IS_SENSITIVE
                } else {
                    "android.content.extra.IS_SENSITIVE"
                },
                true
            )
            return this
        }

        /**
         * 设置一个扩展信息
         */
        fun extra(key: String, value: String): ClipBuilder {
            extras.putString(key, value)
            return this
        }

        /**
         * 设置一个扩展信息
         */
        fun extra(key: String, value: Int): ClipBuilder {
            extras.putInt(key, value)
            return this
        }

        /**
         * 设置一个扩展信息
         */
        fun extra(key: String, value: Boolean): ClipBuilder {
            extras.putBoolean(key, value)
            return this
        }

        /**
         * 设置一个扩展信息
         */
        fun extra(key: String, value: Long): ClipBuilder {
            extras.putLong(key, value)
            return this
        }

        /**
         * 设置一个扩展信息
         */
        fun extra(key: String, value: BooleanArray): ClipBuilder {
            extras.putBooleanArray(key, value)
            return this
        }

        /**
         * 设置一个扩展信息
         */
        fun extra(key: String, value: Double): ClipBuilder {
            extras.putDouble(key, value)
            return this
        }

        /**
         * 设置一个扩展信息
         */
        fun extra(key: String, value: DoubleArray): ClipBuilder {
            extras.putDoubleArray(key, value)
            return this
        }

        /**
         * 设置一个扩展信息
         */
        fun extra(key: String, value: IntArray): ClipBuilder {
            extras.putIntArray(key, value)
            return this
        }

        /**
         * 设置一个扩展信息
         */
        fun extra(key: String, value: LongArray): ClipBuilder {
            extras.putLongArray(key, value)
            return this
        }

        /**
         * 设置一个扩展信息
         */
        fun extra(key: String, value: Array<String>): ClipBuilder {
            extras.putStringArray(key, value)
            return this
        }

        /**
         * 提交本次拷贝内容
         */
        fun commit() {
            try {
                if (clipItemList.isEmpty()) {
                    return
                }
                val clipData = ClipData(
                    ClipDescription(
                        label,
                        mimeTypes.toTypedArray()
                    ),
                    clipItemList[0]
                )
                for (i in 1 until clipItemList.size) {
                    clipData.addItem(clipItemList[i])
                }
                clipData.description.extras = extras
                copy(context, clipData)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

    }

    class PasteHelper(context: Context) {

        private val clipboardManager: ClipboardManager? =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

        /**
         * 剪切板是否有内容
         */
        val hasPrimaryClip: Boolean
            get() {
                return clipboardManager?.hasPrimaryClip() ?: false
            }

        private fun hasMimeType(mimeType: String): Boolean {
            return clipboardManager?.primaryClipDescription?.hasMimeType(mimeType) ?: false
        }

        private fun primaryClip(): ClipData? {
            return clipboardManager?.primaryClip
        }

        /**
         * 查找剪切板上的文本内容
         * 如果剪切板没有文本内容，那么将不会尝试读取，也将不会触发系统的剪切板读取警告
         * 但是他也将只会尝试读取文本类型，如果包含其他类型的剪切内容，也不会返回有效的结果
         */
        fun findText(): List<CharSequence> {
            val list = ArrayList<CharSequence>()
            findClipData(ClipDescription.MIMETYPE_TEXT_PLAIN) {
                it.text?.let { text ->
                    list.add(text)
                }
            }
            return list
        }

        /**
         * 查找剪切板上的HTML内容
         * 同 findText()
         */
        fun findHtml(): List<Pair<CharSequence, String>> {
            val list = ArrayList<Pair<CharSequence, String>>()
            findClipData(ClipDescription.MIMETYPE_TEXT_HTML) {
                val text = it.text
                val html = it.htmlText
                if (text != null || html != null) {
                    list.add(Pair(text ?: "", html ?: ""))
                }
            }
            return list
        }

        /**
         * 查找剪切板上的Intent
         * 同 findText()
         */
        fun findIntent(): List<Intent> {
            val list = ArrayList<Intent>()
            findClipData(ClipDescription.MIMETYPE_TEXT_INTENT) {
                it.intent?.let { intent ->
                    list.add(intent)
                }
            }
            return list
        }

        /**
         * 查找剪切板上的Uri
         * 同 findText()
         */
        fun findUri(): List<Uri> {
            val list = ArrayList<Uri>()
            findClipData(ClipDescription.MIMETYPE_TEXT_URILIST) {
                it.uri?.let { uri ->
                    list.add(uri)
                }
            }
            return list
        }

        /**
         * 查找剪切板上的任何内容
         * 他将不会按照指定的类型查找
         * 那么意味着你可能没有得到想要的数据，但是系统仍然提示用户你读取了剪切板
         */
        fun findAny(): List<ClipData.Item> {
            val list = ArrayList<ClipData.Item>()
            findClipData("") {
                list.add(it)
            }
            return list
        }

        private fun findClipData(
            mimeType: String,
            itemForEach: (ClipData.Item) -> Unit
        ) {
            try {
                if (!hasPrimaryClip) {
                    return
                }
                if (mimeType.isEmpty() || hasMimeType(mimeType)) {
                    val clipData = primaryClip() ?: return
                    val itemCount = clipData.itemCount
                    if (itemCount < 1) {
                        return
                    }
                    for (i in 0 until itemCount) {
                        itemForEach(clipData.getItemAt(i))
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

}