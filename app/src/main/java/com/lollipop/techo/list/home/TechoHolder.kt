package com.lollipop.techo.list.home

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.base.util.onClick
import com.lollipop.techo.data.TechoInfo
import com.lollipop.techo.databinding.ItemHomeTechoBinding
import com.lollipop.techo.util.FontHelper
import com.lollipop.techo.util.RichTextHelper
import com.lollipop.techo.util.setTypeface
import java.util.*

class TechoHolder(
    private val binding: ItemHomeTechoBinding
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(parent: ViewGroup): TechoHolder {
            return TechoHolder(parent.bind())
        }
    }

    private val calendar by lazy {
        Calendar.getInstance()
    }

    private var techoId = 0

    init {
        binding.dayNumberView.setTypeface(FontHelper.Type.HomeListDay)
        binding.monthNumberView.setTypeface(FontHelper.Type.HomeListMonth)
        itemView.onClick {
            onItemViewClick(it.context)
        }
    }

    private fun onItemViewClick(context: Context) {
        context.startActivity(Intent(context, TechoDetailActivity::class.java).apply {
            TechoDetailActivity.putParams(this, techoId)
        })
    }

    fun bind(info: TechoInfo) {
        techoId = info.id
        calendar.timeInMillis = info.updateTime
        binding.dayNumberView.text = calendar.get(Calendar.DAY_OF_MONTH).formatNumber()
        val monthNumber = calendar.get(Calendar.MONTH) + 1
        binding.monthNumberView.text = monthNumber.formatNumber()
        binding.titleInfoView.text = info.title
        RichTextHelper.startRichFlow()
            .addRichInfo(info.items, RichTextHelper.RichOption.TEXT_ALL)
            .into(binding.summaryInfoView)
    }

    private fun Int.formatNumber(): String {
        return if (this < 10) {
            "0${this}"
        } else {
            this.toString()
        }
    }

}