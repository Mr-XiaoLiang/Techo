package com.lollipop.techo.edit.impl

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.list.ItemTouchState
import com.lollipop.base.list.attachTouchHelper
import com.lollipop.base.util.*
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.tintByNotObvious
import com.lollipop.pigment.tintBySelectState
import com.lollipop.techo.R
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.data.TextSpan
import com.lollipop.techo.databinding.ItemPanelTextOptionFrameBinding
import com.lollipop.techo.databinding.PanelTextOptionBinding
import com.lollipop.techo.edit.base.BottomEditDelegate
import com.lollipop.techo.util.RichTextHelper
import com.lollipop.techo.util.TextSelectedHelper
import java.util.*

open class BaseOptionDelegate<T : TechoItem> : BottomEditDelegate<T>(),
    TextSelectedHelper.OnSelectedRangChangedListener {

    private var binding: PanelTextOptionBinding? = null

    override val contentGroup: View?
        get() {
            return binding?.editCard
        }
    override val backgroundView: View?
        get() {
            return binding?.backgroundView
        }

    private var currentTextSpan = TextSpan()

    private val fontStyleHolderList = ArrayList<OptionButtonHelper>(FontStyle.values().size)

    private var selectedHelperPrinter: TextSelectedHelper.Painter? = null

    private val techoItemInfo = TechoItem.Text()

    private val frameAdapter = FontStyleFrameAdapter(techoItemInfo.spans, ::getSpanValue)

    override fun onCreateView(container: ViewGroup): View {
        binding?.let {
            return it.root
        }
        val newBinding: PanelTextOptionBinding = container.bind(false)
        binding = newBinding
        newBinding.editCard.fixInsetsByMargin(WindowInsetsHelper.Edge.ALL)
        return newBinding.root
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        tryUse(binding) {
            clickToClose(it.backgroundView)
            it.editCard.setEmptyClick()
            val selector = TextSelectedHelper.selector()
                .onSelectedChanged(this@BaseOptionDelegate)
                .bind(it.textSelectorView)
            val painter = TextSelectedHelper.printer()
                .setColor(Color.RED)
                .halfRadius()
                .setLayoutProvider { it.textSelectorView }
                .notifyInvalidate { it.textSelectorView.invalidate() }
                .bindTo(selector)
            it.textSelectorView.background = painter
            selectedHelperPrinter = painter

            it.stepListView.adapter = frameAdapter
            it.stepListView.layoutManager = LinearLayoutManager(
                it.stepListView.context, RecyclerView.VERTICAL, false
            )
            it.stepListView.attachTouchHelper()
                .canDrag(true)
                .canSwipe(true)
                .onMove(::onFrameItemMove)
                .onSwipe(::onFrameItemSwipe)
                .onStatusChange(::onFrameItemTouchStateChanged)
                .apply()
            bindOptionButton(it)
        }
    }

    private fun getSpanValue(span: TextSpan): CharSequence {
        val start = span.start
        val end = span.end
        if (start < 0 || end < 0) {
            return ""
        }
        if (start >= end) {
            return ""
        }
        val value = techoItemInfo.value
        if (start >= value.length || end > value.length) {
            return ""
        }
        return value.substring(start, end)
    }

    private fun onFrameItemTouchStateChanged(
        viewHolder: RecyclerView.ViewHolder?,
        status: ItemTouchState
    ) {
        if (status == ItemTouchState.IDLE) {
            updatePreview()
        }
    }

    private fun onFrameItemMove(srcPosition: Int, targetPosition: Int): Boolean {
        val list = techoItemInfo.spans
        val indices = list.indices
        return if (srcPosition in indices && targetPosition in indices) {
            Collections.swap(list, srcPosition, targetPosition)
            true
        } else {
            false
        }
    }

    private fun onFrameItemSwipe(adapterPosition: Int) {
        // TODO
    }

    private fun bindOptionButton(b: PanelTextOptionBinding) {
        fontStyleHolderList.add(
            OptionButtonHelper.bind(
                b.blurOptionBtn,
                FontStyle.Blur,
                ::onFontStyleChanged
            )
        )
        fontStyleHolderList.add(
            OptionButtonHelper.bind(
                b.boldOptionBtn,
                FontStyle.Bold,
                ::onFontStyleChanged
            )
        )
        fontStyleHolderList.add(
            OptionButtonHelper.bind(
                b.strikethroughOptionBtn,
                FontStyle.Strikethrough,
                ::onFontStyleChanged
            )
        )
        fontStyleHolderList.add(
            OptionButtonHelper.bind(
                b.subscriptOptionBtn,
                FontStyle.Subscript,
                ::onFontStyleChanged
            )
        )
        fontStyleHolderList.add(
            OptionButtonHelper.bind(
                b.superscriptOptionBtn,
                FontStyle.Superscript,
                ::onFontStyleChanged
            )
        )
        fontStyleHolderList.add(
            OptionButtonHelper.bind(
                b.italicOptionBtn,
                FontStyle.Italic,
                ::onFontStyleChanged
            )
        )
        fontStyleHolderList.add(
            OptionButtonHelper.bind(
                b.underlinedOptionBtn,
                FontStyle.Underline,
                ::onFontStyleChanged
            )
        )
    }

    private fun onFontStyleChanged(style: FontStyle, has: Boolean) {
        if (has) {
            currentTextSpan.addStyle(style)
        } else {
            currentTextSpan.clearStyle(style)
        }
        updatePreview()
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        tryUse(binding) {
            it.doneBtn.tintByNotObvious(pigment)
            it.optionLinearLayout.let { group ->
                val childCount = group.childCount
                val optionColor = 0xFF333333.toInt()
                for (index in 0 until childCount) {
                    group.getChildAt(index)?.let { child ->
                        if (child.id != R.id.colorOptionBtn && child is ImageView) {
                            child.tintBySelectState(pigment, optionColor)
                        }
                    }
                }
            }
            it.scrollBar.color = pigment.secondary
//            it.textSelectorView.setTextColor(pigment.onSecondaryBody)
        }
        selectedHelperPrinter?.setColor(pigment.secondary.changeAlpha(0.4F))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOpen(info: T) {
        super.onOpen(info)
        info.copyTo(techoItemInfo)
        addFrame()
        tryUse(binding) {
            it.textSelectorView.text = info.value
            frameAdapter.notifyDataSetChanged()
        }
        updatePreview()
    }

    private fun addFrame() {
        val newSpan = TextSpan()
        currentTextSpan = newSpan
        techoItemInfo.spans.add(newSpan)
    }

    override fun onSelectedRangChanged(start: Int, end: Int) {
        currentTextSpan.start = start
        currentTextSpan.end = end
        updatePreview()
    }

    private fun updatePreview() {
        tryUse(binding) {
            RichTextHelper.startRichFlow().addRichInfo(techoItemInfo).into(it.previewView)
        }
    }

    private class OptionButtonHelper(
        private val button: View,
        private val style: FontStyle,
        private val onOptionChanged: (style: FontStyle, has: Boolean) -> Unit
    ) : View.OnClickListener {

        companion object {
            fun bind(
                btn: View,
                style: FontStyle,
                onOptionChanged: (style: FontStyle, has: Boolean) -> Unit
            ): OptionButtonHelper {
                return OptionButtonHelper(btn, style, onOptionChanged)
            }
        }

        init {
            button.setOnClickListener(this)
        }

        fun check(textSpan: TextSpan) {
            button.isSelected = textSpan.hasStyle(style)
        }

        override fun onClick(v: View?) {
            val selected = !button.isSelected
            button.isSelected = selected
            onOptionChanged(style, selected)
        }

    }

    private class FontStyleFrameHolder(
        private val binding: ItemPanelTextOptionFrameBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: CharSequence) {
            binding.labelView.text = text
        }
    }

    private class FontStyleFrameAdapter(
        private val list: List<TextSpan>,
        private val textProvider: (TextSpan) -> CharSequence
    ) : RecyclerView.Adapter<FontStyleFrameHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontStyleFrameHolder {
            return FontStyleFrameHolder(parent.bind())
        }

        override fun onBindViewHolder(holder: FontStyleFrameHolder, position: Int) {
            holder.bind(textProvider(list[position]))
        }

        override fun getItemCount(): Int {
            return list.size
        }

    }

}

class TextOptionDelegate : BaseOptionDelegate<TechoItem.Text>()
class TitleOptionDelegate : BaseOptionDelegate<TechoItem.Title>()
class CheckBoxOptionDelegate : BaseOptionDelegate<TechoItem.CheckBox>()
class NumberOptionDelegate : BaseOptionDelegate<TechoItem.Number>()