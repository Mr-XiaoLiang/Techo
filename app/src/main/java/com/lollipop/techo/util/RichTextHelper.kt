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
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.data.TextSpan
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

        /**
         * Span的Builder的扩展方法，可以直接指定开始位置以及长度的方式来设置span
         * 它允许对同一段文字设置多个sapn
         */
        private fun SpannableStringBuilder.addSpan(
            start: Int,
            length: Int,
            spans: Array<out Any>
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

        fun textSpanBuilder(
            span: TextSpan,
            builder: SpannableStringBuilder
        ): (impl: Array<Any>) -> Unit {
            return {
                builder.addSpan(span.start, span.length, it)
            }
        }

        fun textSpanSimpleBuilder(
            span: TextSpan,
            builder: SpannableStringBuilder
        ): (impl: Any) -> Unit {
            return {
                builder.addSpan(span.start, span.length, arrayOf(it))
            }
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
        info: List<TechoItem>,
        option: RichOption = RichOption.FULL_ALL
    ): RichTextHelper {
        if (option.font.isNone) {
            return this
        }
        info.forEachIndexed { index, item ->
            if (index > 0) {
                addInfo("\n")
            }
            addRichInfo(item, option)
        }
        return this
    }

    fun addRichInfo(
        info: TechoItem,
        option: RichOption = RichOption.FULL_ALL
    ): RichTextHelper {
        if (option.font.isNone) {
            return this
        }

        val allStr = when (info) {
            is TechoItem.CheckBox,
            is TechoItem.Number,
            is TechoItem.Text -> {
                info.value
            }
            is TechoItem.Photo -> {
                "[^_^]"
            }
            is TechoItem.Split -> {
                "[---]"
            }
        }.let {
            if (option.font.isText) {
                it.replace("\n", "")
            } else {
                it
            }
        }

        val spanBuilder = SpannableStringBuilder(allStr)

        if (option.font.isText || !info.spanEnable) {
            // 添加到集合中
            addSpan(spanBuilder)
            return this
        }

        info.spans.forEach { span ->
            addSpan(spanBuilder, span)
        }

        // 添加到集合中
        addSpan(spanBuilder)
        return this
    }

    private fun addSpan(spanBuilder: SpannableStringBuilder, span: TextSpan) {
        val addTextSpan = textSpanSimpleBuilder(span, spanBuilder)
        if (span.hasStyle(FontStyle.FontSize)) {
            // 添加字体大小的Span
            addTextSpan(AbsoluteSizeSpan(span.fontSize, true))
        }
        if (span.hasStyle(FontStyle.Link) && span.link.isNotEmpty()) {
            //设置 link
            hasLink = true
            // 添加一个点击的span
            addTextSpan(ClickSpan(span.color, ClickWrapper(span.link, ClickEvent.LINK, ::onClick)))
        } else if (span.hasStyle(FontStyle.Color)) {
            // 添加一个前景色的span
            addTextSpan(ForegroundColorSpan(span.color))
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
            addTextSpan(StyleSpan(typeface))
        }

        // 设置 下划线
        if (span.hasStyle(FontStyle.Underline)) {
            addTextSpan(UnderlineSpan())
        }

        // 设置 删除线
        if (span.hasStyle(FontStyle.Strikethrough)) {
            addTextSpan(StrikethroughSpan())
        }

        // 设置 上标
        if (span.hasStyle(FontStyle.Superscript)) {
            addTextSpan(SuperscriptSpan())
        }

        // 设置 下标
        if (span.hasStyle(FontStyle.Subscript)) {
            addTextSpan(SubscriptSpan())
        }

        // 设置模糊
        if (span.hasStyle(FontStyle.Blur)) {
            addTextSpan(MaskFilterSpan(BlurMaskFilter(20F, BlurMaskFilter.Blur.NORMAL)))
        }
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
        addSpan(SpannableStringBuilder(value).addSpan(0, value.length, span))
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
    ) {
        companion object {
            val FULL_ALL = RichOption(
                font = DecodeLevel.FULL,
            )
            val TEXT_ALL = RichOption(
                font = DecodeLevel.TEXT,
            )
        }

        fun baseTo(
            font: DecodeLevel = this.font,
        ): RichOption {
            return RichOption(
                font = font,
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