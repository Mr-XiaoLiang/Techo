package com.lollipop.techo.edit.impl.textOption

import android.annotation.SuppressLint
import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lollipop.base.list.ItemTouchState
import com.lollipop.base.list.OnItemMoveCallback
import com.lollipop.base.list.OnItemSwipeCallback
import com.lollipop.base.list.OnItemTouchStateChangedListener
import com.lollipop.techo.R
import com.lollipop.techo.data.FontStyle
import com.lollipop.techo.data.TextSpan
import java.util.*

internal class FrameManager(
    private val spanList: MutableList<TextSpan>,
    private val contextProvider: () -> Activity?,
    private val updatePreview: () -> Unit,
    private val updateOptionButton: () -> Unit,
) : OnItemSwipeCallback, OnItemMoveCallback, OnItemTouchStateChangedListener {

    private var currentInfoValue = ""

    var currentTextSpan = TextSpan()
        private set

    val frameAdapter = FontStyleFrameAdapter(
        spanList,
        ::getSpanValue,
        ::isCurrentSpan,
        { addFrame(true) },
        ::onFrameClick
    )

    @SuppressLint("NotifyDataSetChanged")
    fun init(value: String) {
        currentInfoValue = value
        if (spanList.isEmpty()) {
            addFrame(false)
        }
        frameAdapter.notifyDataSetChanged()
        updatePreview()
        updateOptionButton()
    }

    private fun addFrame(update: Boolean) {
        val newSpan = TextSpan()
        currentTextSpan = newSpan
        spanList.add(newSpan)
        if (update) {
            frameAdapter.notifyItemInserted(spanList.size - 1)
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
        updatePreview()
        updateOptionButton()
    }

    private fun onFrameClick(adapterPosition: Int) {
        val span = spanList[adapterPosition]
        if (span == currentTextSpan) {
            return
        }
        val lastIndex = spanList.indexOf(currentTextSpan)
        currentTextSpan = span
        frameAdapter.notifyItemChanged(adapterPosition)
        frameAdapter.notifyItemChanged(lastIndex)
        updateOptionButton()
    }

    fun changeCurrentStyle(style: FontStyle, has: Boolean) {
        if (has) {
            currentTextSpan.addStyle(style)
        } else {
            currentTextSpan.clearStyle(style)
        }
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
        val list = spanList
        val indices = list.indices
        val result = if (srcPosition in indices && targetPosition in indices) {
            Collections.swap(list, srcPosition, targetPosition)
            true
        } else {
            false
        }
        updatePreview()
        return result
    }

    override fun onItemTouchStateChanged(
        viewHolder: RecyclerView.ViewHolder?,
        status: ItemTouchState
    ) {
        if (status == ItemTouchState.IDLE) {
            updatePreview()
            updateOptionButton()
        }
    }

}