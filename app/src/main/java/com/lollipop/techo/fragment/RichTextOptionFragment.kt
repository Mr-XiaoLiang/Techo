package com.lollipop.techo.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.slider.Slider
import com.lollipop.base.util.*
import com.lollipop.palette.ColorHistoryHelper
import com.lollipop.palette.ColorWheelView
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.tintByNotObvious
import com.lollipop.techo.R
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.FragmentRichTextOptionBinding
import com.lollipop.techo.databinding.ItemColorHistroyBinding
import com.lollipop.techo.edit.impl.textOption.FrameManager
import com.lollipop.techo.util.RichTextHelper
import com.lollipop.techo.util.TextSelectedHelper
import kotlin.math.max
import kotlin.math.min

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

    private var selectorContentHeight = 0
    private var selectorGroupHeight = 0

    private val colorHistory by lazy {
        ColorHistoryHelper(requireContext())
    }

    private val colorHistoryAdapter by lazy {
        ColorHistoryAdapter(::onColorSelected)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        binding.textSelectorScrollView.addListener(::onSelectorTextHeightChanged)
        binding.textSelectorScrollBar.addListener(::scrollTextSelector)

        with(binding.palettePresetListView) {
            layoutManager = LinearLayoutManager(context)
            adapter = colorHistoryAdapter
        }
        initInsets()
    }

    private fun initInsets() {

        val rightAndBottom = WindowInsetsHelper.Edge.build {
            right = WindowInsetsHelper.EdgeStrategy.COMPARE
            bottom = WindowInsetsHelper.EdgeStrategy.ACCUMULATE
        }

        binding.textSelectorView.fixInsetsByPadding(
            WindowInsetsHelper.Edge.build {
                bottom = WindowInsetsHelper.EdgeStrategy.ACCUMULATE
            }
        )
        binding.textSelectorScrollBar.fixInsetsByPadding(rightAndBottom)
        binding.layerPanel.fixInsetsByPadding(rightAndBottom)
        binding.richOptionGroup.fixInsetsByPadding(rightAndBottom)
        binding.palettePanel.fixInsetsByPadding(rightAndBottom)
        binding.fontSizePresetGroup.fixInsetsByPadding(rightAndBottom)

        val header = WindowInsetsHelper.Edge.build {
            top = WindowInsetsHelper.EdgeStrategy.ACCUMULATE
            left = WindowInsetsHelper.EdgeStrategy.ACCUMULATE
            right = WindowInsetsHelper.EdgeStrategy.ACCUMULATE
        }
        binding.backButton.fixInsetsByMargin(header)
        binding.previewView.fixInsetsByPadding(header)

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        colorHistory.load {
            colorHistoryAdapter.reset(colorHistory.list)
        }
    }

    private fun onColorSelected(color: Int) {
        frameManager.onCurrentSpanColorChanged(color)
        binding.colorWheelView.reset(color)
        binding.palettePreviewView.setBackgroundColor(color)
    }

    private fun onSelectorTextHeightChanged(contentHeight: Int, height: Int) {
        selectorContentHeight = contentHeight
        selectorGroupHeight = height
        if (contentHeight <= height) {
            binding.textSelectorScrollBar.isVisible = false
        } else {
            binding.textSelectorScrollBar.isVisible = true
            binding.textSelectorScrollBar.contentWeight = height * 1F / contentHeight
            binding.textSelectorScrollBar.progress = 0F
            scrollTextSelector(0F)
        }
    }

    private fun scrollTextSelector(progress: Float) {
        val p = max(0F, min(1F, progress))
        val offsetY = ((selectorContentHeight - selectorGroupHeight) * p).toInt()
        binding.textSelectorScrollView.scrollTo(0, offsetY)
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
            it.doneBtn.tintByNotObvious(pigment)
            it.textSelectorScrollBar.color = pigment.secondary
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
            donePanel.isVisible = false
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
            R.id.menuDone -> {
                binding.donePanel.isVisible = true
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

    private class ColorHistoryAdapter(
        private val onColorClick: (color: Int) -> Unit
    ) : RecyclerView.Adapter<ColorHistoryHolder>() {

        private val list = ArrayList<Int>()

        @SuppressLint("NotifyDataSetChanged")
        fun reset(set: Set<Int>) {
            list.clear()
            list.addAll(set)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorHistoryHolder {
            return ColorHistoryHolder(parent.bind(), ::onHolderClick)
        }

        private fun onHolderClick(position: Int) {
            onColorClick(list[position])
        }

        override fun onBindViewHolder(holder: ColorHistoryHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int {
            return list.size
        }

    }

    private class ColorHistoryHolder(
        private val binding: ItemColorHistroyBinding,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.colorView.setOnClickListener {
                onHolderClick()
            }
        }

        private fun onHolderClick() {
            onClick(adapterPosition)
        }

        fun bind(color: Int) {
            binding.colorView.setBackgroundColor(color)
        }

    }

}