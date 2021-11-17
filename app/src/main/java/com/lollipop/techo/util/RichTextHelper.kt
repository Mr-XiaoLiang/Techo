package com.lollipop.techo.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.PointF
import android.graphics.Typeface
import android.text.*
import android.text.style.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.lollipop.base.util.dp2px
import com.lollipop.techo.data.BaseTextItem
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TextItem
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * @author lollipop
 * @date 2021/10/11 20:33
 */
class RichTextHelper {

    companion object {
        /**
         * 从空白开始一个富翁本解析工作
         */
        fun startRichFlow(): RichTextHelper {
            return RichTextHelper()
        }

    }

    /**
     * span信息的集合
     */
    private val spanList = ArrayList<CharSequence>()

    /**
     * 用于设置TextView的链接相关的属性
     * 理论上构建的过程不需要TextView参与，
     * 但是部分富文本的设置需要对View的属性进行设置，因此此处进行记录，
     * 当必要的时候，进行设置
     */
    var hasLink = false
        private set

    /**
     * 点击事件的聚合接口
     */
    private var onClickListener: OnClickListener? = null

    /**
     * 添加一个点击的内容
     */
    fun <T : Any> addClickInfo(
        value: String,
        textColor: Int,
        info: T,
        type: ClickEvent,
        clickListener: (T, ClickEvent) -> Unit
    ): RichTextHelper {
        hasLink = true
        addSpan(
            value,
            ForegroundColorSpan(textColor),
            ClickSpan(textColor, ClickWrapper(info, type, clickListener))
        )
        return this
    }

    /**
     * 添加标准的富文本信息
     */
    fun addRichInfo(
        info: List<TextItem>,
        option: RichOption = RichOption.FULL_ALL
    ): RichTextHelper {
        if (option.font.isNone) {
            return this
        }
        // 如果需要清理文本中的空格
        if (option.font.isText && option.abbreviateSpace) {
            abbreviateSpace(info, option)
        }
        info.forEach {
            addRichInfo(it, option, true)
        }
        return this
    }

    /**
     * 添加标准的富文本信息
     */
    fun addRichInfo(
        info: BaseTextItem,
        option: RichOption = RichOption.FULL_ALL
    ): RichTextHelper {
        return addRichInfo(info, option, false)
    }

    private fun addRichInfo(
        info: BaseTextItem,
        option: RichOption = RichOption.FULL_ALL,
        inner: Boolean
    ): RichTextHelper {
        if (option.font.isNone) {
            return this
        }
        // 如果需要清理文本中的空格
        if (!inner && option.font.isText && option.abbreviateSpace) {
            abbreviateSpace(listOf(info), option)
        }

        var allStr = info.value

        if (option.font.isText) {
            allStr = allStr.replace("\n", "")
        }

        val spanBuilder = SpannableStringBuilder(allStr)

        if (option.font.isText) {
            // 添加到集合中
            addSpan(spanBuilder)
            return this
        }

        info.spans.forEach { span ->
            val spanStart = span.start
            val spanLength = span.length
            if (span.hasStyle(FontStyle.FontSize)) {
                // 添加字体大小的Span
                spanBuilder.addSpan(
                    spanStart,
                    spanLength,
                    AbsoluteSizeSpan(span.fontSize, true)
                )
            }
            if (span.hasStyle(FontStyle.Link) && span.link.isNotEmpty()) {
                //设置 link
                hasLink = true
                // 添加一个点击的span
                spanBuilder.addSpan(
                    spanStart,
                    spanLength,
                    ClickSpan(
                        span.color,
                        ClickWrapper(span.link, ClickEvent.LINK, ::onClick)
                    )
                )
            } else if (span.hasStyle(FontStyle.Color)) {
                // 添加一个前景色的span
                spanBuilder.addSpan(
                    spanStart,
                    spanLength,
                    ForegroundColorSpan(span.color)
                )
            }
            //设置 粗体& 斜体
            val typeface = when {
                FontStyle.hasAll(span.style, FontStyle.Bold, FontStyle.Italic) -> {
                    Typeface.BOLD_ITALIC
                }
                FontStyle.has(span.style, FontStyle.Bold) -> {
                    Typeface.BOLD
                }
                FontStyle.has(span.style, FontStyle.Italic) -> {
                    Typeface.ITALIC
                }
                else -> {
                    Typeface.NORMAL
                }
            }
            if (typeface != Typeface.NORMAL) {
                spanBuilder.addSpan(
                    spanStart,
                    spanLength,
                    StyleSpan(typeface)
                )
            }

            // 设置 下划线
            if (span.hasStyle(FontStyle.Underline)) {
                spanBuilder.addSpan(
                    spanStart,
                    spanLength,
                    UnderlineSpan()
                )
            }

            // 设置 删除线
            if (span.hasStyle(FontStyle.Strikethrough)) {
                spanBuilder.addSpan(
                    spanStart,
                    spanLength,
                    StrikethroughSpan()
                )
            }

            // 设置 上标
            if (span.hasStyle(FontStyle.Superscript)) {
                spanBuilder.addSpan(
                    spanStart,
                    spanLength,
                    SuperscriptSpan()
                )
            }

            // 设置 下标
            if (span.hasStyle(FontStyle.Subscript)) {
                spanBuilder.addSpan(
                    spanStart,
                    spanLength,
                    SubscriptSpan()
                )
            }

            // 设置模糊
            if (span.hasStyle(FontStyle.Blur)) {
                spanBuilder.addSpan(
                    spanStart,
                    spanLength,
                    MaskFilterSpan(BlurMaskFilter(20F, BlurMaskFilter.Blur.NORMAL))
                )
            }
        }

        // 添加到集合中
        addSpan(spanBuilder)
        return this
    }

    /**
     * 分析并处理富文本结构体的空格信息
     * 1. 如果是字符串内容，那么将会进行
     */
    private fun abbreviateSpace(
        richList: List<BaseTextItem>,
        richOption: RichOption
    ) {
        var isFirstText = true
        var lastText: BaseTextItem? = null
        var previousHasSpace = false
        richList.forEach {
            previousHasSpace = abbreviateSpace(
                it,
                richOption,
                isFirstText,
                false,
                previousHasSpace
            )
            isFirstText = false
            lastText = it
        }
        if (richOption.lastSpace == SpaceLevel.NONE) {
            lastText?.let {
                abbreviateSpace(
                    it,
                    richOption,
                    isFirst = false,
                    isLast = true,
                    preHasSpace = previousHasSpace
                )
            }
        }
    }

    private fun abbreviateSpace(
        info: BaseTextItem,
        richOption: RichOption,
        isFirst: Boolean,
        isLast: Boolean,
        preHasSpace: Boolean
    ): Boolean {
        // 空白字符的检查范围：换行、回车、空格
        val space = "[\\n\\r ]"

        var newString = info.value
        // 清空前置的所有空白字符
        if (isFirst) {
            newString = replaceString(newString, "^$space*", richOption.firstSpace)
        }
        // 如果前面已经有空格了，那么也需要清理全部空格，但是需要做合并操作
        // 前面留下空格，只有两种情况，要么是做了合并，要么保留，如果保留，那么我们接着保留，
        // 否则的话，合并为一个，但是前面已经有了，所以我们需要直接清理
        if (preHasSpace) {
            newString = replaceString(
                newString,
                "^$space*",
                if (richOption.contentSpace.isKeep) {
                    SpaceLevel.KEEP
                } else {
                    SpaceLevel.NONE
                }
            )
        }
        // 清理末尾的所有空格元素
        if (isLast) {
            newString = replaceString(newString, "$space*$", richOption.lastSpace)
        }
        // 处理内容中的元素，如果是合并，那么是大于一个的时候，才需要合并
        // 如果是不显示，那么直接清理所有的空格
        newString = replaceString(
            newString,
            if (richOption.contentSpace.isNone) {
                space
            } else {
                "$space+"
            }, richOption.contentSpace
        )

        info.value = newString

        // 处理内部的富文本
//        abbreviateSpace(info.text, richOption)

        return newString.endsWith(" ")
    }

    private fun replaceString(string: String, pattern: String, spaceLevel: SpaceLevel): String {
        if (spaceLevel.isKeep) {
            return string
        }
        return string.replace(
            Regex(pattern),
            if (spaceLevel.isNone) {
                ""
            } else {
                " "
            }
        )
    }

    /**
     * 添加一个原始的内容信息
     */
    fun addInfo(info: CharSequence): RichTextHelper {
        addSpan(info)
        return this
    }

    /**
     * 添加一个Icon小标识
     * @param iconId Icon的资源id
     * @param iconWidth icon的宽度，单位dp
     * @param iconHeight icon的高度，单位dp
     * @param keyword 富文本对应的关键字信息
     */
    fun addIconFlag(
        context: Context,
        @DrawableRes
        iconId: Int,
        iconWidth: Int,
        iconHeight: Int,
        keyword: String,
        keepLength: Int = 0
    ): RichTextHelper {
        ContextCompat.getDrawable(context, iconId)?.let { drawable ->
            drawable.setBounds(0, 0.dp2px, iconWidth.dp2px, iconHeight.dp2px)
            addSpan(SpannableStringBuilder(keyword).apply {
                setSpan(
                    CenterAlignImageSpan(drawable),
                    0,
                    keyword.length - keepLength,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            })
        }
        return this
    }

    /**
     * 用于部分需要判断条件的可选富文本条件
     */
    fun optional(value: Boolean, callback: RichTextHelper.() -> Unit): RichTextHelper {
        if (value) {
            callback(this)
        }
        return this
    }

    /**
     * 添加一个Span到集合中
     */
    private fun addSpan(charSequence: CharSequence) {
        spanList.add(charSequence)
    }

    /**
     * 添加一个字符串以及它的span信息
     * 将会为它创建一个Builder，完成组合后添加到span的集合中
     * 允许对同一段位置设置多个span
     */
    private fun addSpan(
        value: String,
        vararg span: Any
    ) {
        addSpan(SpannableStringBuilder(value).addSpan(0, value.length, *span))
    }

    /**
     * Span的Builder的扩展方法，可以直接指定开始位置以及长度的方式来设置span
     * 它允许对同一段文字设置多个sapn
     */
    private fun SpannableStringBuilder.addSpan(
        start: Int,
        length: Int,
        vararg spans: Any
    ): SpannableStringBuilder {
        val startIndex = min(this.length, max(start, 0))
        val endIndex = min(this.length, max(length + startIndex, 0))
        for (span in spans) {
            try {
                setSpan(span, startIndex, endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return this
    }

    /**
     * 绑定一个点击的监听器
     * @param listener 监听的回调函数
     */
    fun onClick(listener: OnClickListener): RichTextHelper {
        this.onClickListener = listener
        return this
    }

    private fun onClick(data: String, event: ClickEvent) {
        onClickListener?.onClick(data, event)
    }

    private fun getColor(context: Context, @ColorRes colorId: Int): Int {
        return ContextCompat.getColor(context, colorId)
    }

    /**
     * 将现有的调整内容，转换为TextView可以使用的内容
     */
    fun build(): SpannableStringBuilder {
        val spannableBuilder = SpannableStringBuilder()
        spanList.forEach {
            spannableBuilder.append(it)
        }
        return spannableBuilder
    }

    /**
     * 将自身绑定到TextView中，并且提供一些默认到属性设置
     */
    fun into(textView: TextView) {
        val spannableText = build()
        textView.text = spannableText
        if (hasLink) {
            SpanClickHelper.bindTo(textView, spannableText, true)
        } else {
            SpanClickHelper.clean(textView)
        }
    }

    /**
     * 聚合的点击事件接口
     */
    fun interface OnClickListener {
        fun onClick(data: String, event: ClickEvent)
    }

    private class ClickSpan<T : Any>(
        private val textColor: Int,
        private val clickListener: ClickWrapper<T>
    ) : ClickableSpan() {

        override fun onClick(widget: View) {
            clickListener.invoke()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = if (textColor == 0) {
                ds.linkColor
            } else {
                textColor
            }
            ds.isUnderlineText = false
        }
    }

    private data class ClickWrapper<T : Any>(
        private val data: T,
        private val event: ClickEvent = ClickEvent.LINK,
        private val listener: (T, ClickEvent) -> Unit
    ) {

        fun invoke() {
            listener.invoke(data, event)
        }

    }

    enum class ClickEvent {
        LINK,
    }

    /**
     * 富文本解析的控制开关
     * 可以指定解析富文本的类型
     */
    data class RichOption(
        val font: DecodeLevel = DecodeLevel.TEXT,
        val firstSpace: SpaceLevel = SpaceLevel.KEEP,
        val lastSpace: SpaceLevel = SpaceLevel.KEEP,
        val contentSpace: SpaceLevel = SpaceLevel.KEEP
    ) {
        companion object {
            val FULL_ALL = RichOption(
                font = DecodeLevel.FULL,
                firstSpace = SpaceLevel.KEEP,
                lastSpace = SpaceLevel.KEEP,
                contentSpace = SpaceLevel.KEEP
            )
            val TEXT_ALL = RichOption(
                font = DecodeLevel.TEXT,
                firstSpace = SpaceLevel.NONE,
                lastSpace = SpaceLevel.NONE,
                contentSpace = SpaceLevel.MERGE
            )
        }

        val abbreviateSpace: Boolean
            get() {
                return firstSpace != SpaceLevel.KEEP ||
                        lastSpace != SpaceLevel.KEEP ||
                        contentSpace != SpaceLevel.KEEP
            }

        fun baseTo(
            font: DecodeLevel = this.font,
            firstSpace: SpaceLevel = this.firstSpace,
            lastSpace: SpaceLevel = this.lastSpace,
            contentSpace: SpaceLevel = this.contentSpace
        ): RichOption {
            return RichOption(
                font = font,
                firstSpace = firstSpace,
                lastSpace = lastSpace,
                contentSpace = contentSpace
            )
        }

    }

    /**
     * 富文本解析的级别
     * 按照目前的需求，分为三个级别
     * 完整解析，文本显示，完全隐藏
     */
    enum class DecodeLevel {
        /**
         * 完全解析
         */
        FULL,

        /**
         * 文本显示
         * （如图片、视频等，会转换为[图片]、[视频]等内容显示
         */
        TEXT,

        /**
         * 完全不显示
         */
        NONE;

        val isFull: Boolean
            get() {
                return this == FULL
            }

        val isText: Boolean
            get() {
                return this == TEXT
            }

        val isNone: Boolean
            get() {
                return this == NONE
            }
    }

    /**
     * 空格的等级
     * 分为保留，合并，不显示三个级别
     */
    enum class SpaceLevel {
        KEEP, MERGE, NONE;

        val isKeep: Boolean
            get() {
                return this == KEEP
            }

        val isMerge: Boolean
            get() {
                return this == MERGE
            }

        val isNone: Boolean
            get() {
                return this == NONE
            }
    }

    /**
     * 为了解决TextView布局以及ClickSpan的矛盾问题，
     * 所以使用TouchListener来手动触发Span的点击事件
     * 问题场景：常规情况下使用ClickSpan，需要设置setMovementMethod，
     * 但是此方法会导致TextView的最大行数设置产生的末尾省略号丢失，
     * 并且TextView会发生滑动，导致在列表中文本显示不完整。
     * 因此使用OnTouchListener来代替LinkMovementMethod
     */
    private class SpanClickHelper
    private constructor(
        private val spannable: Spannable,
        private val touchSlop: Int,
        private val clickFirst: Boolean
    ) : View.OnTouchListener {

        companion object {
            @SuppressLint("ClickableViewAccessibility")
            fun bindTo(textView: TextView, spannable: Spannable, clickFirst: Boolean) {
                textView.setOnTouchListener(
                    SpanClickHelper(
                        spannable,
                        ViewConfiguration.get(textView.context).scaledTouchSlop,
                        clickFirst
                    )
                )
            }

            @SuppressLint("ClickableViewAccessibility")
            fun clean(textView: TextView) {
                textView.setOnTouchListener(null)
            }
        }

        private var cancelClick = false
        private val touchDownLocation = PointF()

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            v ?: return false
            if (v !is TextView) {
                return false
            }
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    // 按下时，重制状态，并且如果此时没有被选中的ClickableSpan也直接放弃
                    cancelClick = false
                    touchDownLocation.set(event.x, event.y)
                    val offset = getSpanOffset(v, event)
                    val spans = spannable.getSpans(offset, offset, ClickableSpan::class.java)
                    if (spans.isNotEmpty()) {
                        Selection.setSelection(
                            spannable,
                            spannable.getSpanStart(spans[0]),
                            spannable.getSpanEnd(spans[0])
                        )
                    } else {
                        cancelClick = true
                        Selection.removeSelection(spannable)
                        return false
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    // 多指操作，放弃点击事件
                    cancelClick = true
                    Selection.removeSelection(spannable)
                    return false
                }
                MotionEvent.ACTION_MOVE -> {
                    // 如果已经放弃，那么就直接返回，如果没有，那么就检查手指的移动范围，
                    // 超过范围之后，认为发生滑动事件，放弃点击事件的触发
                    if (cancelClick) {
                        return false
                    }
                    val x = event.x
                    val y = event.y
                    // 滑动超过阈值，放弃事件
                    if (abs(touchDownLocation.x - x) > touchSlop
                        || abs(touchDownLocation.y - y) > touchSlop
                    ) {
                        cancelClick = true
                        Selection.removeSelection(spannable)
                        return false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    // 如果已经被放弃事件，那么不再处理
                    if (cancelClick) {
                        return false
                    }
                    // 获取点击位置相对于Span的偏移量，以此来获取点击的span，并且触发它
                    val offset = getSpanOffset(v, event)
                    val spans = spannable.getSpans(offset, offset, ClickableSpan::class.java)
                    if (spans.isNotEmpty()) {
                        if (clickFirst) {
                            spans[0].onClick(v)
                        } else {
                            spans.forEach {
                                it.onClick(v)
                            }
                        }
                    }
                    Selection.removeSelection(spannable)
                }
            }
            return true
        }

        private fun getSpanOffset(v: TextView, event: MotionEvent): Int {
            var x = event.x
            var y = event.y.toInt()
            x -= v.totalPaddingLeft
            y -= v.totalPaddingTop
            x += v.scrollX
            y += v.scrollY

            val layout = v.layout
            val lineForVertical = layout.getLineForVertical(y)
            return layout.getOffsetForHorizontal(lineForVertical, x)
        }

    }

}