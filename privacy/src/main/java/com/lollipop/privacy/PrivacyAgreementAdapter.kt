package com.lollipop.privacy

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PrivacyAgreementAdapter(
    private val dataList: List<PrivacyAgreementItem>,
    private val holderCreator: (ViewGroup) -> PrivacyAgreementHolder,
) : RecyclerView.Adapter<PrivacyAgreementHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivacyAgreementHolder {
        return holderCreator(parent)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: PrivacyAgreementHolder, position: Int) {
        holder.bind(dataList[position])
    }

}