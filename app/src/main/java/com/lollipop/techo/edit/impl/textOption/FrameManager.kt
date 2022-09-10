package com.lollipop.techo.edit.impl.textOption

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lollipop.base.list.*
import com.lollipop.base.util.bind
import com.lollipop.techo.R
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.data.TextSpan
import com.lollipop.techo.databinding.ItemPanelTextOptionFrameAddBinding
import com.lollipop.techo.databinding.ItemPanelTextOptionFrameBinding
import org.json.JSONObject
import java.util.*

internal class FrameManager(
    private val contextProvider: () -> Activity?,
    private val updatePreview: () -> Unit,
    private val updateOptionButton: () -> Unit,
) : OnItemSwipeCallback, OnItemMoveCallback, OnItemTouchStateChangedListener {

    val techoItemInfo = TechoItem.Text()

    private val currentInfoValue: String
        get() {
            return techoItemInfo.value
        }

    var currentTextSpan = TextSpan()
        private set

    private val spanList = ArrayList<TextSpan>()

    private val frameAdapter = FontStyleFrameAdapter(
        spanList,
        ::getSpanValue,
        ::isCurrentSpan,
        { addFrame(true) },
        ::onFrameClick
    )

    fun init(infoJson: String) {
        if (infoJson.isEmpty()) {
            return
        }
        try {
            val jsonObject = JSONObject(infoJson)
            techoItemInfo.parse(jsonObject)
            onInfoReset()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun init(info: TechoItem) {
        info.copyTo(techoItemInfo)
        onInfoReset()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onInfoReset() {
        spanList.clear()
        if (spanList.isEmpty()) {
            addFrame(false)
        }
        frameAdapter.notifyDataSetChanged()

        syncSpan()

        updatePreview()
        updateOptionButton()
    }

    fun bindTo(recyclerView: RecyclerView) {
        recyclerView.adapter = frameAdapter
        recyclerView.layoutManager = LinearLayoutManager(
            recyclerView.context, RecyclerView.VERTICAL, false
        )
        recyclerView.attachTouchHelper()
            .canDrag(true)
            .canSwipe(true)
            .onMove(this)
            .onSwipe(this)
            .onStatusChange(this)
            .apply()
    }

    fun onCurrentSpanFontSizeChanged(size: Int) {
        currentTextSpan.addStyle(FontStyle.FontSize)
        currentTextSpan.fontSize = size
        updatePreview()
    }

    fun onCurrentSpanRangeChanged(start: Int, end: Int) {
        currentTextSpan.start = start
        currentTextSpan.end = end
        val index = spanList.indexOf(currentTextSpan)
        if (index < 0) {
            return
        }
        frameAdapter.callChanged(index)
        updatePreview()
    }

    private fun syncSpan() {
        val outList = techoItemInfo.spans
        outList.clear()
        spanList.forEach {
            outList.add(0, it)
        }
    }

    private fun addFrame(update: Boolean) {
        val newSpan = TextSpan()
        val lastCurrent = currentTextSpan
        currentTextSpan = newSpan
        spanList.add(0, newSpan)
        val lastSpanPosition = spanList.indexOf(lastCurrent)
        syncSpan()
        if (update) {
            frameAdapter.callInsert(0)
            if (lastSpanPosition >= 0) {
                frameAdapter.callChanged(lastSpanPosition)
            }
            updatePreview()
            updateOptionButton()
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
        spanList.removeAt(frameAdapter.positionOffset(adapterPosition))
        frameAdapter.notifyItemRemoved(adapterPosition)
        if (spanList.indexOf(currentTextSpan) < 0) {
            if (spanList.isEmpty()) {
                addFrame(false)
                frameAdapter.callInsert(0)
            } else {
                val position = 0
                currentTextSpan = spanList[position]
                frameAdapter.callChanged(position)
            }
        }
        syncSpan()
        updatePreview()
        updateOptionButton()
    }

    private fun onFrameClick(dataPosition: Int) {
        val span = spanList[dataPosition]
        if (span == currentTextSpan) {
            return
        }
        val lastIndex = spanList.indexOf(currentTextSpan)
        currentTextSpan = span
        frameAdapter.callChanged(dataPosition)
        frameAdapter.callChanged(lastIndex)
        updateOptionButton()
    }

    fun changeCurrentStyle(callback: (TextSpan) -> Unit) {
        callback(currentTextSpan)
        updatePreview()
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
        val dataSrc = srcPosition - 1
        val dataTarget = targetPosition - 1
        val list = spanList
        val indices = list.indices
        val result = if (dataSrc in indices && dataTarget in indices) {
            Collections.swap(list, dataSrc, dataTarget)
            true
        } else {
            false
        }
        frameAdapter.notifyItemMoved(srcPosition, targetPosition)
        syncSpan()
        updatePreview()
        return result
    }

    override fun onItemTouchStateChanged(
        viewHolder: RecyclerView.ViewHolder?,
        status: ItemTouchState
    ) {
        if (status == ItemTouchState.IDLE) {
            syncSpan()
            updatePreview()
            updateOptionButton()
        }
    }

    internal class FontStyleFrameAdapter(
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
                    FontStyleFrameHolder(parent.bind(), ::onItemClick)
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is FontStyleFrameHolder) {
                val textSpan = list[positionOffset(position)]
                holder.bind(textProvider(textSpan), isSelected(textSpan))
            }
        }

        private fun onItemClick(position: Int) {
            onFrameClick(position - 1)
        }

        fun callInsert(dataPosition: Int) {
            notifyItemInserted(dataPosition + 1)
        }

        fun callChanged(dataPosition: Int) {
            notifyItemChanged(dataPosition + 1)
        }

        fun positionOffset(position: Int): Int {
            return position - 1
        }

        override fun getItemViewType(position: Int): Int {
            if (position == 0) {
                return STYLE_ADD
            }
            return STYLE_FRAME
        }

        override fun getItemCount(): Int {
            return list.size + 1
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

}