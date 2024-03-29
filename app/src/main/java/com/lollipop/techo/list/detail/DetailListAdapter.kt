package com.lollipop.techo.list.detail

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.techo.data.AppTheme
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.data.TechoItemType
import com.lollipop.techo.data.TechoTheme

/**
 * @author lollipop
 * @date 2021/12/6 22:45
 */
class DetailListAdapter(
    private val data: List<TechoItem>,
    private val onItemOptionButtonClickListener: EditHolder.OnItemOptionButtonClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isInEdit = false

    private var itemPigment: TechoTheme.Snapshot = TechoTheme.valueOf(AppTheme.current)

    private fun findPigment(): TechoTheme.Snapshot {
        return itemPigment
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder = when (TechoItemType.entries[viewType]) {
            TechoItemType.Title -> TitleInfoHolder.create(parent)
            TechoItemType.Text,
            TechoItemType.Number,
            TechoItemType.CheckBox -> TextInfoHolder.create(parent)

            TechoItemType.Photo -> PhotoInfoHolder.create(parent)
            TechoItemType.Split -> SplitInfoHolder.create(parent)
            TechoItemType.Recording -> {
                TODO()
            }

            TechoItemType.Vcr -> {
                TODO()
            }
        }
        holder.setOnItemOptionButtonClickListener(onItemOptionButtonClickListener)
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val info = data[position]
        when (holder) {
            is TextInfoHolder -> {
                when (info) {
                    is TechoItem.Text -> holder.bind(info)
                    is TechoItem.Number -> holder.bind(info)
                    is TechoItem.CheckBox -> holder.bind(info)
                    else -> {}
                }
            }

            is TitleInfoHolder -> {
                if (info is TechoItem.Title) {
                    holder.bind(info)
                }
            }

            is PhotoInfoHolder -> {
                if (info is TechoItem.Photo) {
                    holder.bind(info)
                }
            }

            is SplitInfoHolder -> {
                if (info is TechoItem.Split) {
                    holder.bind(info)
                }
            }
        }
        if (holder is EditHolder<*>) {
            holder.onEditModeChange(isInEdit)
            holder.updateDecoration(itemPigment)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onDecorationChanged(techoTheme: TechoTheme.Snapshot) {
        this.itemPigment = techoTheme
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeEditMode(isInEdit: Boolean) {
        this.isInEdit = isInEdit
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].itemType.ordinal
    }

}