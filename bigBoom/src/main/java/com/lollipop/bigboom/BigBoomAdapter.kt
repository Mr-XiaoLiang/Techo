package com.lollipop.bigboom

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BigBoomAdapter(
    private val valueList: List<String>,
    private val statusCallback: SelectedStatusCallback,
    private val itemProvider: PatchesItemProvider,
) : RecyclerView.Adapter<PatchesHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatchesHolder {
        return itemProvider.createHolder(parent)
    }

    override fun onBindViewHolder(holder: PatchesHolder, position: Int) {
        holder.bind(valueList[position], statusCallback.isSelected(position))
    }

    override fun getItemCount(): Int {
        return valueList.size
    }

    fun interface SelectedStatusCallback {
        fun isSelected(position: Int): Boolean
    }

}