package com.lollipop.privacy

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

abstract class PrivacyAgreementHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(item: PrivacyAgreementItem) {
        getLabelView()?.setText(item.label)
        getContentView()?.setText(item.content)
        onBind(item)
    }

    protected abstract fun getLabelView(): TextView?

    protected abstract fun getContentView(): TextView?

    protected open fun onBind(item: PrivacyAgreementItem) {}

}