package com.lollipop.techo.list.detail

import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.lollipop.base.list.TouchableHolder
import com.lollipop.base.util.bind
import com.lollipop.base.util.onClick
import com.lollipop.techo.databinding.ItemEditGroupBinding

/**
 * @author lollipop
 * @date 2021/11/13 22:09
 */
open class EditHolder<T : ViewBinding>(
    protected val binding: EditItemView<T>
) : RecyclerView.ViewHolder(binding.option.root), TouchableHolder {

    companion object {

        inline fun <reified T : ViewBinding> ViewGroup.bindContent(): EditItemView<T> {
            val optionBinding = bind<ItemEditGroupBinding>()
            val contentBinding = optionBinding.itemContentGroup.bind<T>()
            return EditItemView(contentBinding, optionBinding)
        }

    }

    private val optionBinding: ItemEditGroupBinding = binding.option

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
        optionBinding.moreOptionView.onClick {
            onOptionButtonClick()
        }
        optionBinding.itemContentGroup.addView(binding.content.root)
    }

    fun setOnItemOptionButtonClickListener(listener: OnItemOptionButtonClickListener) {
        this.optionButtonClickListener = listener
    }

    fun onEditModeChange(isInEditMode: Boolean) {
        this.isInEditMode = isInEditMode
        optionBinding.dragHandlerView.isInvisible = !isInEditMode
        optionBinding.moreOptionView.isInvisible = !isInEditMode
    }

    protected fun callOnEditClick() {
        optionButtonClickListener?.onItemEditButtonClick(this)
    }

    private fun onOptionButtonClick() {
        optionButtonClickListener?.onItemOptionButtonClick(this)
    }

    interface OnItemOptionButtonClickListener {
        fun onItemOptionButtonClick(holder: EditHolder<*>)
        fun onItemEditButtonClick(holder: EditHolder<*>)
    }

    class EditItemView<C : ViewBinding>(
        val content: C,
        val option: ItemEditGroupBinding
    )

}