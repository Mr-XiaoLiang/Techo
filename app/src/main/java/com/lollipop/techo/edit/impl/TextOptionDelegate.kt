package com.lollipop.techo.edit.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lollipop.base.list.ItemTouchState
import com.lollipop.base.list.OnItemMoveCallback
import com.lollipop.base.list.OnItemSwipeCallback
import com.lollipop.base.list.attachTouchHelper
import com.lollipop.base.util.*
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.tintByNotObvious
import com.lollipop.pigment.tintBySelectState
import com.lollipop.techo.R
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.data.TextSpan
import com.lollipop.techo.databinding.ItemPanelTextOptionFrameAddBinding
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

    private val fontStyleHolderList = ArrayList<OptionButtonHelper>(FontStyle.values().size)

    private var selectedHelperPrinter: TextSelectedHelper.Painter? = null

    private val techoItemInfo = TechoItem.Text()

    private val frameManager = FrameManager(techoItemInfo.spans)

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
                .onSwipe(::onFrameItemSwipe)
                .onStatusChange(::onFrameItemTouchStateChanged)
                .apply()
            bindOptionButton(it)
        }
    }

    private fun onFrameItemTouchStateChanged(
        viewHolder: RecyclerView.ViewHolder?,
        status: ItemTouchState
    ) {
        if (status == ItemTouchState.IDLE) {
            updatePreview()
            updateFontStyleButton()
        }
    }

    private fun onFrameItemSwipe(adapterPosition: Int) {

    }

    private fun removeFrame(adapterPosition: Int) {
        techoItemInfo.spans.removeAt(adapterPosition)
        frameAdapter.notifyItemRemoved(adapterPosition)
        if (techoItemInfo.spans.indexOf(currentTextSpan) < 0) {
            if (techoItemInfo.spans.isEmpty()) {
                addFrame()
            } else {
                currentTextSpan = techoItemInfo.spans[techoItemInfo.spans.size - 1]
                updateFontStyleButton()
            }
        }
        updatePreview()
    }

    private fun onFrameClick(adapterPosition: Int) {
        val span = techoItemInfo.spans[adapterPosition]
        if (span == currentTextSpan) {
            return
        }
        val lastIndex = techoItemInfo.spans.indexOf(currentTextSpan)
        currentTextSpan = span
        frameAdapter.notifyItemChanged(adapterPosition)
        frameAdapter.notifyItemChanged(lastIndex)
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

    private fun updateFontStyleButton() {
        val textSpan = currentTextSpan
        fontStyleHolderList.forEach {
            it.check(textSpan)
        }
    }

    private fun onFontStyleChanged(style: FontStyle, has: Boolean) {
        if (has) {
            frameManager.currentTextSpan.addStyle(style)
        } else {
            frameManager.currentTextSpan.clearStyle(style)
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
        private val binding: ItemPanelTextOptionFrameBinding,
        private val onItemClickListener: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { onItemClickListener(adapterPosition) }
        }

        fun bind(text: CharSequence, isSelected: Boolean) {
            binding.labelView.text = text
            binding.selectedIcon.isVisible = isSelected
        }
    }

    private class AddFontStyleFrameHolder(
        binding: ItemPanelTextOptionFrameAddBinding,
        private val onItemClickListener: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener { onItemClickListener() }
        }
    }

    private class FontStyleFrameAdapter(
        private val list: List<TextSpan>,
        private val textProvider: (TextSpan) -> CharSequence,
        private val isSelected: (TextSpan) -> Boolean,
        private val onAddButtonClick: () -> Unit,
        private val onFrameClick: (Int) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val STYLE_ADD = 0
            private const val STYLE_FRAME = 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                STYLE_ADD -> {
                    AddFontStyleFrameHolder(parent.bind(), onAddButtonClick)
                }
                else -> {
                    FontStyleFrameHolder(parent.bind(), onFrameClick)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is FontStyleFrameHolder) {
                val textSpan = list[position]
                holder.bind(textProvider(textSpan), isSelected(textSpan))
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position >= list.size) {
                return STYLE_ADD
            }
            return STYLE_FRAME
        }

        override fun getItemCount(): Int {
            return list.size + 1
        }

    }

    private class FrameManager(
        private val spanList: MutableList<TextSpan>,
        private val notifyFrameChanged: () -> Unit,
        private val onFrameClick: (Int) -> Unit,
        private val contextProvider: () -> Activity?
    ) : OnItemSwipeCallback, OnItemMoveCallback {

        private var currentInfoValue = ""

        var currentTextSpan = TextSpan()
            private set

        val frameAdapter = FontStyleFrameAdapter(
            spanList,
            ::getSpanValue,
            ::isCurrentSpan,
            { addFrame(true) },
            onFrameClick
        )

        @SuppressLint("NotifyDataSetChanged")
        fun init(value: String) {
            currentInfoValue = value
            if (spanList.isEmpty()) {
                addFrame(false)
            }
            frameAdapter.notifyDataSetChanged()
            notifyFrameChanged()
        }

        private fun addFrame(update: Boolean) {
            val newSpan = TextSpan()
            currentTextSpan = newSpan
            spanList.add(newSpan)
            if (update) {
                frameAdapter.notifyItemInserted(spanList.size - 1)
                notifyFrameChanged()
            }
        }

        private fun isCurrentSpan(span: TextSpan): Boolean {
            return currentTextSpan == span
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
            val value = currentInfoValue
            if (start >= value.length || end > value.length) {
                return ""
            }
            return value.substring(start, end)
        }

        private fun removeFrame(adapterPosition: Int) {
            spanList.removeAt(adapterPosition)
            frameAdapter.notifyItemRemoved(adapterPosition)
            if (spanList.indexOf(currentTextSpan) < 0) {
                if (spanList.isEmpty()) {
                    addFrame(false)
                    frameAdapter.notifyItemInserted(0)
                } else {
                    val position = spanList.size - 1
                    currentTextSpan = spanList[position]
                    frameAdapter.notifyItemChanged(position)
                }
            }
            notifyFrameChanged()
        }

        override fun onSwipe(adapterPosition: Int) {
            val c = contextProvider() ?: return
            MaterialAlertDialogBuilder(c)
                .setTitle(R.string.title_remove_font_style_frame)
                .setMessage(R.string.message_remove_font_style_frame)
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                    frameAdapter.notifyItemChanged(adapterPosition)
                }
                .setPositiveButton(R.string.remove) { dialog, _ ->
                    dialog.dismiss()
                    removeFrame(adapterPosition)
                }
                .show()
        }

        override fun onMove(srcPosition: Int, targetPosition: Int): Boolean {
            val list = spanList
            val indices = list.indices
            return if (srcPosition in indices && targetPosition in indices) {
                Collections.swap(list, srcPosition, targetPosition)
                true
            } else {
                false
            }
        }

    }

}

class TextOptionDelegate : BaseOptionDelegate<TechoItem.Text>()
class TitleOptionDelegate : BaseOptionDelegate<TechoItem.Title>()
class CheckBoxOptionDelegate : BaseOptionDelegate<TechoItem.CheckBox>()
class NumberOptionDelegate : BaseOptionDelegate<TechoItem.Number>()