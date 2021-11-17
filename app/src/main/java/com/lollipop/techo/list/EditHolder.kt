package com.lollipop.techo.list

import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.list.TouchableHolder
import com.lollipop.base.util.bind
import com.lollipop.techo.databinding.ItemEditGroupBinding

/**
 * @author lollipop
 * @date 2021/11/13 22:09
 */
open class EditHolder(
    private val optionBinding: ItemEditGroupBinding
) : RecyclerView.ViewHolder(optionBinding.root), TouchableHolder {

    companion object {
        fun createItemView(
            group: ViewGroup,
        ): ItemEditGroupBinding {
            return group.bind(true)
        }

        fun getContentGroup(optionBinding: ItemEditGroupBinding): ViewGroup {
            return optionBinding.itemContentGroup
        }
    }

    private var isInEditMode = false

    private var optionButtonClickListener: OnItemOptionButtonClickListener? = null

    override val canDrag: Boolean
        get() {
            return isInEditMode
        }
    override val canSwipe: Boolean
        get() {
            return isInEditMode
        }

    init {
        onEditModeChange(false)
        optionBinding.moreOptionView.setOnClickListener {
            onOptionButtonClick()
        }
    }

    fun setOnItemOptionButtonClickListener(listener: OnItemOptionButtonClickListener) {
        this.optionButtonClickListener = listener
    }

    fun onEditModeChange(isInEditMode: Boolean) {
        this.isInEditMode = isInEditMode
        optionBinding.dragHandlerView.isInvisible = !isInEditMode
        optionBinding.moreOptionView.isInvisible = !isInEditMode
    }

    private fun onOptionButtonClick() {
        optionButtonClickListener?.onItemOptionButtonClick(this)
    }

    fun interface OnItemOptionButtonClickListener {
        fun onItemOptionButtonClick(holder: EditHolder)
    }

}