package com.lollipop.techo.edit.impl

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.slider.Slider
import com.lollipop.base.util.*
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.tintByNotObvious
import com.lollipop.pigment.tintBySelectState
import com.lollipop.techo.R
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.PanelTextOptionBinding
import com.lollipop.techo.edit.base.BottomEditDelegate
import com.lollipop.techo.edit.impl.textOption.FrameManager
import com.lollipop.techo.edit.impl.textOption.OptionButtonHelper
import com.lollipop.techo.util.RichTextHelper
import com.lollipop.techo.util.TextSelectedHelper

open class BaseOptionDelegate<T : TechoItem> : BottomEditDelegate<T>(),
    TextSelectedHelper.OnSelectedRangeChangedListener {

    private var binding: PanelTextOptionBinding? = null

    override val contentGroup: View?
        get() {
            return binding?.editCard
        }
    override val backgroundView: View?
        get() {
            return binding?.backgroundView
        }

    private var selectedHelperPrinter: TextSelectedHelper.Painter? = null

    private val frameManager = FrameManager(
        ::context,
        ::updatePreview,
        ::updateFontStyleButton
    )

    private val optionButtonHelper = OptionButtonHelper { style, has ->
        frameManager.changeCurrentStyle(style, has)
    }

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

            it.fontSizeSlider.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    frameManager.onCurrentSpanFontSizeChanged(value.toInt())
                }
            })

            frameManager.bindTo(it.stepListView)
            bindOptionButton(it)
        }
    }

    private fun bindOptionButton(b: PanelTextOptionBinding) {
        optionButtonHelper.bind(
            b.blurOptionBtn to FontStyle.Blur,
            b.boldOptionBtn to FontStyle.Bold,
            b.strikethroughOptionBtn to FontStyle.Strikethrough,
            b.subscriptOptionBtn to FontStyle.Subscript,
            b.superscriptOptionBtn to FontStyle.Superscript,
            b.italicOptionBtn to FontStyle.Italic,
            b.underlinedOptionBtn to FontStyle.Underline,
        )
    }

    private fun updateFontStyleButton() {
        tryUse(frameManager.currentTextSpan) {
            optionButtonHelper.check(it)
            binding?.fontSizeSlider?.value = it.fontSize.toFloat()
            selectedHelperPrinter?.setSelectedRange(it.start, it.end)
        }
    }

    private fun onFontStyleChanged(style: FontStyle, has: Boolean) {
        frameManager.changeCurrentStyle(style, has)
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        tryUse(binding) {
            it.doneBtn.tintByNotObvious(pigment)
            it.optionLinearLayout.let { group ->
                val childCount = group.childCount
                val optionColor = it.root.context.getColor(R.color.text_theme)
                for (index in 0 until childCount) {
                    group.getChildAt(index)?.let { child ->
                        if (child is ImageView) {
                            child.tintBySelectState(pigment, optionColor)
                        }
                    }
                }
            }
            it.scrollBar.color = pigment.secondary
            it.fontSizeSlider.thumbTintList = ColorStateList.valueOf(pigment.secondary)
            it.fontSizeSlider.trackTintList = ColorStateList.valueOf(pigment.secondaryVariant)
        }
        selectedHelperPrinter?.setColor(pigment.secondary.changeAlpha(0.4F))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOpen(info: T) {
        super.onOpen(info)
        tryUse(binding) {
            it.textSelectorView.text = info.value
        }
        frameManager.init(info)
    }

    override fun onSelectedRangeChanged(start: Int, end: Int) {
        frameManager.onCurrentSpanRangeChanged(start, end)
    }

    private fun updatePreview() {
        tryUse(binding) {
            RichTextHelper.startRichFlow()
                .addRichInfo(frameManager.techoItemInfo)
                .into(it.previewView)
        }
    }

}

class TextOptionDelegate : BaseOptionDelegate<TechoItem.Text>()
class TitleOptionDelegate : BaseOptionDelegate<TechoItem.Title>()
class CheckBoxOptionDelegate : BaseOptionDelegate<TechoItem.CheckBox>()
class NumberOptionDelegate : BaseOptionDelegate<TechoItem.Number>()