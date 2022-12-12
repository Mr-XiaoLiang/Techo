package com.lollipop.base.util

import android.content.*
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle

object Clipboard {

    fun copy(context: Context, label: CharSequence = "Text", value: String) {
        copy(context, ClipData.newPlainText(label, value))
    }

    fun copy(
        context: Context,
        resolver: ContentResolver,
        label: CharSequence = "URI",
        value: Uri
    ) {
        copy(context, ClipData.newUri(resolver, label, value))
    }

    fun copy(context: Context, label: CharSequence = "Intent", value: Intent) {
        copy(context, ClipData.newIntent(label, value))
    }

    fun copy(context: Context, label: CharSequence = "Html", value: CharSequence, html: String) {
        copy(context, ClipData.newHtmlText(label, value, html))
    }

    fun copy(context: Context, label: CharSequence = "URI", value: Uri) {
        copy(context, ClipData.newRawUri(label, value))
    }

    fun copy(context: Context): ClipBuilder {
        return ClipBuilder(context)
    }

    fun paste(context: Context): PasteHelper {
        return PasteHelper(context)
    }

    private fun copy(context: Context, clipInfo: ClipData) {
        val clipboardService = context.getSystemService(Context.CLIPBOARD_SERVICE)
        if (clipboardService is ClipboardManager) {
            clipboardService.setPrimaryClip(clipInfo)
        }
    }

    class ClipBuilder(private val context: Context) {

        private var label: CharSequence = ""
        private var mimeTypes = HashSet<String>()
        private val clipItemList = ArrayList<ClipData.Item>()
        private val extras = PersistableBundle()

        fun label(value: CharSequence): ClipBuilder {
            this.label = value
            return this
        }

        private fun addItem(mimeType: String, item: ClipData.Item) {
            clipItemList.add(item)
            mimeTypes.add(mimeType)
        }

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

        fun copy(value: Intent): ClipBuilder {
            addItem(ClipDescription.MIMETYPE_TEXT_INTENT, ClipData.Item(value))
            return this
        }

        fun copy(value: CharSequence, html: String): ClipBuilder {
            addItem(ClipDescription.MIMETYPE_TEXT_HTML, ClipData.Item(value, html))
            return this
        }

        fun copy(value: Uri): ClipBuilder {
            addItem(ClipDescription.MIMETYPE_TEXT_URILIST, ClipData.Item(value))
            return this
        }

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

        fun extra(key: String, value: String): ClipBuilder {
            extras.putString(key, value)
            return this
        }

        fun extra(key: String, value: Int): ClipBuilder {
            extras.putInt(key, value)
            return this
        }

        fun extra(key: String, value: Boolean): ClipBuilder {
            extras.putBoolean(key, value)
            return this
        }

        fun extra(key: String, value: Long): ClipBuilder {
            extras.putLong(key, value)
            return this
        }

        fun extra(key: String, value: BooleanArray): ClipBuilder {
            extras.putBooleanArray(key, value)
            return this
        }

        fun extra(key: String, value: Double): ClipBuilder {
            extras.putDouble(key, value)
            return this
        }

        fun extra(key: String, value: DoubleArray): ClipBuilder {
            extras.putDoubleArray(key, value)
            return this
        }

        fun extra(key: String, value: IntArray): ClipBuilder {
            extras.putIntArray(key, value)
            return this
        }

        fun extra(key: String, value: LongArray): ClipBuilder {
            extras.putLongArray(key, value)
            return this
        }

        fun extra(key: String, value: Array<String>): ClipBuilder {
            extras.putStringArray(key, value)
            return this
        }

        fun commit() {
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
        }

    }

    class PasteHelper(context: Context) {

        private val clipboardManager: ClipboardManager? =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

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

        fun findText(): List<CharSequence> {
            val list = ArrayList<CharSequence>()
            findClipData(ClipDescription.MIMETYPE_TEXT_PLAIN) {
                it.text?.let { text ->
                    list.add(text)
                }
            }
            return list
        }

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

        fun findIntent(): List<Intent> {
            val list = ArrayList<Intent>()
            findClipData(ClipDescription.MIMETYPE_TEXT_INTENT) {
                it.intent?.let { intent ->
                    list.add(intent)
                }
            }
            return list
        }

        fun findUri(): List<Uri> {
            val list = ArrayList<Uri>()
            findClipData(ClipDescription.MIMETYPE_TEXT_URILIST) {
                it.uri?.let { uri ->
                    list.add(uri)
                }
            }
            return list
        }

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