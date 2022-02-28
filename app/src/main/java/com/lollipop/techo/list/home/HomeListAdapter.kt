package com.lollipop.techo.list.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.techo.data.TechoInfo

class HomeListAdapter(
    private val data: List<TechoInfo>
) : RecyclerView.Adapter<TechoHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechoHolder {
        return TechoHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TechoHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}