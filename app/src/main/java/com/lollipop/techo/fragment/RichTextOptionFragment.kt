package com.lollipop.techo.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.slider.Slider
import com.lollipop.base.util.*
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsEdgeStrategy
import com.lollipop.base.util.insets.fixInsetsByMargin
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.palette.ColorHistoryHelper
import com.lollipop.palette.ColorWheelView
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.tintByNotObvious
import com.lollipop.techo.R
import com.lollipop.techo.activity.SingleFragmentActivity
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.FragmentRichTextOptionBinding
import com.lollipop.techo.databinding.ItemColorHistroyBinding
import com.lollipop.techo.edit.impl.textOption.FrameManager
import com.lollipop.techo.util.RichTextHelper
import com.lollipop.techo.util.TextSelectedHelper
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

class RichTextOptionFragment : PageFragment(),
    ColorWheelView.OnColorChangedListener,
    TextSelectedHelper.OnSelectedRangeChangedListener {

    companion object {

        val LAUNCHER: Class<out SingleFragmentActivity.LaunchContract<Request, Result>> = ActivityLauncherImpl::class.java

        private const val ARG_INFO_JSON = "ARG_INFO_JSON"

        private const val ARG_KEY = "ARG_KEY"

        private const val RESULT_INFO_JSON = "RESULT_INFO_JSON"

        private const val RESULT_SRC_ARGUMENTS = "RESULT_SRC_ARGUMENTS"

        private fun createFragmentArguments(result: Request): Bundle {
            return Bundle().apply {
                putInt(ARG_KEY, result.key)
                putString(ARG_INFO_JSON, result.info.toJson().toString())
            }
        }

        private fun getResult(data: Intent): Result {
            val info = data.getStringExtra(RESULT_INFO_JSON) ?: ""
            return Result(
                info.isNotEmpty(),
                data.getIntExtra(ARG_KEY, 0),
                data.getStringExtra(RESULT_SRC_ARGUMENTS) ?: "",
                info,
            )
        }

        private fun createResultData(fragment: RichTextOptionFragment, info: String): Intent {
            val arguments = fragment.arguments
            return Intent().apply {
                putExtra(ARG_KEY, arguments?.getInt(ARG_KEY, 0))
                putExtra(RESULT_SRC_ARGUMENTS, arguments?.getString(ARG_INFO_JSON))
                putExtra(RESULT_INFO_JSON, info)
            }
        }

        private fun getInputInfo(fragment: RichTextOptionFragment): String {
            return fragment.arguments?.getString(ARG_INFO_JSON) ?: ""
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
        ColorHistoryAdapter(colorHistory.list, ::onColorSelected)
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
        frameManager.init(getInputInfo(this))
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
        binding.doneBtn.setOnClickListener {
            setResult()
            notifyBackPress()
        }
        with(binding.palettePresetListView) {
            layoutManager = LinearLayoutManager(context)
            adapter = colorHistoryAdapter
        }
        initInsets()
    }

    private fun initInsets() {

        val rightAndBottom = WindowInsetsEdge.build {
            right = WindowInsetsEdgeStrategy.COMPARE
            bottom = WindowInsetsEdgeStrategy.ACCUMULATE
        }

        binding.textSelectorView.fixInsetsByPadding(
            WindowInsetsEdge.build {
                bottom = WindowInsetsEdgeStrategy.ACCUMULATE
            }
        )
        binding.textSelectorScrollBar.fixInsetsByPadding(rightAndBottom)
        binding.layerPanel.fixInsetsByPadding(rightAndBottom)
        binding.richOptionGroup.fixInsetsByPadding(rightAndBottom)
        binding.palettePanel.fixInsetsByPadding(rightAndBottom)
        binding.fontSizePresetGroup.fixInsetsByPadding(rightAndBottom)

        val header = WindowInsetsEdge.build {
            top = WindowInsetsEdgeStrategy.ACCUMULATE
            left = WindowInsetsEdgeStrategy.ACCUMULATE
            right = WindowInsetsEdgeStrategy.ACCUMULATE
        }
        binding.backButton.fixInsetsByMargin(header)
        binding.previewView.fixInsetsByPadding(header)

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        colorHistory.load {
            colorHistoryAdapter.notifyDataSetChanged()
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

    @SuppressLint("NotifyDataSetChanged")
    private fun onPanelSelected(itemId: Int): Boolean {
        val isPalettePanel = binding.palettePanel.isVisible
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
                colorHistoryAdapter.notifyDataSetChanged()
            }
            R.id.menuDone -> {
                binding.donePanel.isVisible = true
            }
            else -> {
                return false
            }
        }
        if (!binding.palettePanel.isVisible && isPalettePanel) {
            colorHistory.add(frameManager.currentTextSpan.color)
            colorHistory.save()
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

    private fun setResult() {
        val inputJson = getInputInfo(this)
        val inputObj = try {
            JSONObject(inputJson)
        } catch (e: Throwable) {
            JSONObject()
        }
        val resultObj = frameManager.techoItemInfo.toJson()
        resultObj.keys().forEach { key ->
            inputObj.put(key, resultObj.opt(key))
        }
        setResultSuccess(createResultData(this, inputObj.toString()))
    }

    private class RichOption(
        val viewId: Int,
        val fontStyle: FontStyle
    )

    private class ColorHistoryAdapter(
        private val list: List<Int>,
        private val onColorClick: (color: Int) -> Unit
    ) : RecyclerView.Adapter<ColorHistoryHolder>() {

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

    class ActivityLauncherImpl : SingleFragmentActivity.LaunchContract<Request, Result>() {

        override fun parseResult(resultCode: Int, intent: Intent?): Result {
            if (resultCode != Activity.RESULT_OK || intent == null) {
                return Result(false, 0, "", "")
            }
            return getResult(intent)
        }

        override fun createArguments(input: Request): Bundle {
            return createFragmentArguments(input)
        }

        override fun getTarget(input: Request): Class<out Fragment> {
            return RichTextOptionFragment::class.java
        }

    }

    class Request(val key: Int, val info: TechoItem)

    class Result(val success: Boolean, val key: Int, val input: String, val info: String)

}