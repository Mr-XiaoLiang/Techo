package com.lollipop.techo.list.home

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.techo.data.TechoInfo
import com.lollipop.techo.databinding.ItemHomeTechoBinding

class TechoHolder(
    private val binding: ItemHomeTechoBinding
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(parent: ViewGroup): TechoHolder {
            return TechoHolder(parent.bind())
        }
    }

    fun bind(info: TechoInfo) {
        TODO()
    }

}