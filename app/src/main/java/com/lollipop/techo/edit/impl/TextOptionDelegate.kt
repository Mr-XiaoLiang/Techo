package com.lollipop.techo.edit.impl

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.list.attachTouchHelper
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

    private var selectedHelperPrinter: TextSelectedHelper.Painter? = null

    private val techoItemInfo = TechoItem.Text()

    private val frameManager = FrameManager(
        techoItemInfo.spans,
        { context },
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

            it.stepListView.adapter = frameManager.frameAdapter
            it.stepListView.layoutManager = LinearLayoutManager(
                it.stepListView.context, RecyclerView.VERTICAL, true
            )
            it.stepListView.attachTouchHelper()
                .canDrag(true)
                .canSwipe(true)
                .onMove(frameManager)
                .onSwipe(frameManager)
                .onStatusChange(frameManager)
                .apply()
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
        optionButtonHelper.check(frameManager.currentTextSpan)
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
        tryUse(binding) {
            it.textSelectorView.text = info.value
        }
        frameManager.init(info.value)
        updatePreview()
    }

    override fun onSelectedRangChanged(start: Int, end: Int) {
        frameManager.currentTextSpan.start = start
        frameManager.currentTextSpan.end = end
        updatePreview()
    }

    private fun updatePreview() {
        tryUse(binding) {
            RichTextHelper.startRichFlow().addRichInfo(techoItemInfo).into(it.previewView)
        }
    }

}

class TextOptionDelegate : BaseOptionDelegate<TechoItem.Text>()
class TitleOptionDelegate : BaseOptionDelegate<TechoItem.Title>()
class CheckBoxOptionDelegate : BaseOptionDelegate<TechoItem.CheckBox>()
class NumberOptionDelegate : BaseOptionDelegate<TechoItem.Number>()