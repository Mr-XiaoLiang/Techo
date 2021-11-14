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
    }

    fun onEditModeChange(isInEditMode: Boolean) {
        this.isInEditMode = isInEditMode
        optionBinding.dragHandlerView.isInvisible = !isInEditMode
        optionBinding.moreOptionView.isInvisible = !isInEditMode
    }

}