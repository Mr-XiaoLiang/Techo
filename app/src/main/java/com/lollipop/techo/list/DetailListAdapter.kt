package com.lollipop.techo.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.techo.data.*
import com.lollipop.techo.data.TechoItemType.*

/**
 * @author lollipop
 * @date 2021/12/6 22:45
 */
class DetailListAdapter(
    private val data: List<BaseTechoItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (values()[viewType]) {
            Empty -> EmptyHolder.create(parent)
            Text,
            Number,
            CheckBox -> TextInfoHolder.create(parent)
            Photo -> PhotoInfoHolder.create(parent)
            Split -> SplitInfoHolder.create(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val info = data[position]
        when (holder) {
            is EmptyHolder -> {}
            is TextInfoHolder -> {
                when (info) {
                    is TextItem -> holder.bind(info)
                    is NumberItem -> holder.bind(info)
                    is CheckBoxItem -> holder.bind(info)
                }
            }
            is PhotoInfoHolder -> {
                if (info is PhotoItem) {
                    holder.bind(info)
                }
            }
            is SplitInfoHolder -> {
                if (info is SplitItem) {
                    holder.bind(info)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].itemType.ordinal
    }

}