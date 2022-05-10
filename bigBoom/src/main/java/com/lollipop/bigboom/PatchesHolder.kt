package com.lollipop.bigboom

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.onClick

abstract class PatchesHolder(view: View): RecyclerView.ViewHolder(view) {

    private var onHolderClickListener: OnHolderClickListener? = null

    protected open fun bindClickEvent() {
        itemView.onClick {
            callOnHolderClick()
        }
    }

    fun bindClickListener(listener: OnHolderClickListener?) {
        this.onHolderClickListener = listener
        bindClickEvent()
    }

    abstract fun bind(value: String, status: PatchesStatus)

    fun callOnHolderClick() {
        onHolderClickListener?.onHolderClick(this)
    }

    fun interface OnHolderClickListener {
        fun onHolderClick(holder: PatchesHolder)
    }

}