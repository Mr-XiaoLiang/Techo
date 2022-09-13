package com.lollipop.techo.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.slider.Slider
import com.lollipop.base.util.*
import com.lollipop.palette.ColorWheelView
import com.lollipop.pigment.Pigment
import com.lollipop.techo.R
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.FragmentRichTextOptionBinding
import com.lollipop.techo.edit.impl.textOption.FrameManager
import com.lollipop.techo.util.RichTextHelper
import com.lollipop.techo.util.TextSelectedHelper

class RichTextOptionFragment : PageFragment(),
    ColorWheelView.OnColorChangedListener,
    TextSelectedHelper.OnSelectedRangeChangedListener {

    companion object {

        private const val ARG_INFO_JSON = "ARG_INFO_JSON"

        fun createArguments(bundle: Bundle, info: TechoItem) {
            bundle.apply {
                putString(ARG_INFO_JSON, info.toJson().toString())
            }
        }
    }

    private val binding: FragmentRichTextOptionBinding by lazyBind()

    private val frameManager = FrameManager(
        { activity },
        ::onPreviewChanged,
        ::onSelectedRichOptionChanged
    )

    private val richButtonList by lazy {
        arrayOf(
            RichOption(R.id.boldOptionButton, FontStyle.Bold),
            RichOption(R.id.blurOptionButton, FontStyle.Blur),
            RichOption(R.id.italicOptionButton, FontStyle.Italic),
            RichOption(R.id.subscriptOptionButton, FontStyle.Subscript),
            RichOption(R.id.underlinedOptionButton, FontStyle.Underline),
            RichOption(R.id.superscriptOptionButton, FontStyle.Superscript),
            RichOption(R.id.strikethroughOptionButton, FontStyle.Strikethrough),
        )
    }

    private var selectedHelperPrinter: TextSelectedHelper.Painter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButton.fixInsetsByMargin(WindowInsetsHelper.Edge.HEADER)
        bindBackButton(binding.backButton)
        initView()
        initInfo()
    }

    private fun initInfo() {
        frameManager.init(arguments?.getString(ARG_INFO_JSON, "") ?: "")
        binding.textSelectorView.text = frameManager.techoItemInfo.value
    }

    private fun initView() {
        binding.panelMenuBar.setOnItemSelectedListener {
            onPanelSelected(it.itemId)
        }
        binding.richOptionGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            onStyleCheckedIdsChanged(checkedIds)
        }
        binding.fontSizeSlider.addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
            if (fromUser) {
                onFontSizeChanged(value.toInt())
            }
        })
        frameManager.bindTo(binding.layerPanel)
        binding.fontSizePresetGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            onFontSizeCheckedIdsChanged(checkedIds)
        }
        binding.colorWheelView.setOnColorChangedListener(this)
        val selector = TextSelectedHelper.selector()
            .onSelectedChanged(this)
            .bind(binding.textSelectorView)
        val painter = TextSelectedHelper.printer()
            .setColor(Color.RED)
            .halfRadius()
            .setLayoutProvider { binding.textSelectorView }
            .notifyInvalidate { binding.textSelectorView.invalidate() }
            .bindTo(selector)
        binding.textSelectorView.background = painter
        selectedHelperPrinter = painter

        binding.panelMenuBar.selectedItemId = R.id.menuSelector
    }

    private fun onPreviewChanged() {
        tryUse(binding) {
            RichTextHelper.startRichFlow()
                .addRichInfo(frameManager.techoItemInfo)
                .into(it.previewView)
        }
    }

    private fun onSelectedRichOptionChanged() {
        tryUse(frameManager.currentTextSpan) { span ->
            val style = span.style
            richButtonList.forEach { option ->
                binding.richOptionGroup.findViewById<Chip>(option.viewId)?.let { view ->
                    view.isChecked = option.fontStyle.valueIn(style)
                }
            }
            binding.fontSizeSlider.value = span.fontSize.toFloat()
            binding.colorWheelView.reset(span.color)
            binding.palettePreviewView.setBackgroundColor(span.color)
            selectedHelperPrinter?.setSelectedRange(span.start, span.end)
        }
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        tryUse(binding) {
//            it.doneBtn.tintByNotObvious(pigment)
//            it.optionLinearLayout.let { group ->
//                val childCount = group.childCount
//                val optionColor = it.root.context.getColor(R.color.text_theme)
//                for (index in 0 until childCount) {
//                    group.getChildAt(index)?.let { child ->
//                        if (child is ImageView) {
//                            child.tintBySelectState(pigment, optionColor)
//                        }
//                    }
//                }
//            }
//            it.scrollBar.color = pigment.secondary
            it.fontSizeSlider.thumbTintList = ColorStateList.valueOf(pigment.secondary)
            it.fontSizeSlider.trackTintList = ColorStateList.valueOf(pigment.secondaryVariant)
        }
        selectedHelperPrinter?.setColor(pigment.secondary.changeAlpha(0.4F))
    }

    private fun onStyleCheckedIdsChanged(checkedIds: List<Int>) {
        frameManager.changeCurrentStyle { span ->
            richButtonList.forEach { option ->
                if (checkedIds.contains(option.viewId)) {
                    span.addStyle(option.fontStyle)
                } else {
                    span.clearStyle(option.fontStyle)
                }
            }
        }
    }

    private fun onFontSizeCheckedIdsChanged(checkedIds: List<Int>) {
        if (checkedIds.isEmpty()) {
            return
        }
        val fontSize = when (checkedIds[0]) {
            binding.fontSize12Button.id -> {
                12
            }
            binding.fontSize14Button.id -> {
                14
            }
            binding.fontSize16Button.id -> {
                16
            }
            binding.fontSize18Button.id -> {
                18
            }
            binding.fontSize22Button.id -> {
                22
            }
            binding.fontSize24Button.id -> {
                24
            }
            binding.fontSize26Button.id -> {
                26
            }
            else -> {
                return
            }
        }
        binding.fontSizeSlider.value = fontSize.toFloat()
        onFontSizeChanged(fontSize)
    }

    private fun onFontSizeChanged(sp: Int) {
        frameManager.onCurrentSpanFontSizeChanged(sp)
        tryWith(binding) {
            fontSize12Button.isChecked = sp == 12
            fontSize14Button.isChecked = sp == 14
            fontSize16Button.isChecked = sp == 16
            fontSize18Button.isChecked = sp == 18
            fontSize22Button.isChecked = sp == 22
            fontSize24Button.isChecked = sp == 24
            fontSize26Button.isChecked = sp == 26
        }
    }

    private fun onPanelSelected(itemId: Int): Boolean {
        with(binding) {
            selectorPanel.isVisible = false
            layerPanel.isVisible = false
            richOptionPanel.isVisible = false
            palettePanel.isVisible = false
            textSizePanel.isVisible = false
        }
        when (itemId) {
            R.id.menuSelector -> {
                binding.selectorPanel.isVisible = true
            }
            R.id.menuLayer -> {
                binding.layerPanel.isVisible = true
            }
            R.id.menuFontSize -> {
                binding.textSizePanel.isVisible = true
            }
            R.id.menuRichStyle -> {
                binding.richOptionPanel.isVisible = true
            }
            R.id.menuPalette -> {
                binding.palettePanel.isVisible = true
            }
            else -> {
                return false
            }
        }
        return true
    }

    override fun onColorChanged(h: Float, s: Float, v: Float, a: Float) {
        val color = Color.HSVToColor(floatArrayOf(h, s, v))
        binding.palettePreviewView.setBackgroundColor(color)
        frameManager.onCurrentSpanColorChanged(color)
    }

    override fun onSelectedRangeChanged(start: Int, end: Int) {
        frameManager.onCurrentSpanRangeChanged(start, end)
    }

    private class RichOption(
        val viewId: Int,
        val fontStyle: FontStyle
    )

}