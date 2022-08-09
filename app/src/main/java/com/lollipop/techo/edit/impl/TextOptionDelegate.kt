package com.lollipop.techo.edit.impl

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.lollipop.base.util.*
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.tintByNotObvious
import com.lollipop.pigment.tintBySelectState
import com.lollipop.techo.R
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.data.TextSpan
import com.lollipop.techo.databinding.PanelTextOptionBinding
import com.lollipop.techo.edit.base.BottomEditDelegate
import com.lollipop.techo.util.RichTextHelper
import com.lollipop.techo.util.TextSelectedHelper

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

    private var techoItemInfo: TechoItem.Text = TechoItem.Text()

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
            bindOptionButton(it)
        }
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

    override fun onOpen(info: T) {
        super.onOpen(info)
        info.copyTo(techoItemInfo)
        addFrame()
        tryUse(binding) {
            it.textSelectorView.text = info.value
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

}

class TextOptionDelegate : BaseOptionDelegate<TechoItem.Text>()
class TitleOptionDelegate : BaseOptionDelegate<TechoItem.Title>()
class CheckBoxOptionDelegate : BaseOptionDelegate<TechoItem.CheckBox>()
class NumberOptionDelegate : BaseOptionDelegate<TechoItem.Number>()