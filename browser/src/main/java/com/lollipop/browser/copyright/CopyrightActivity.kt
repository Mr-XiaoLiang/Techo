package com.lollipop.browser.copyright

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.lollipop.browser.base.HeaderActivity

abstract class CopyrightActivity<H : ViewHolder, D : Any> : HeaderActivity() {

    protected val recyclerView by lazy { RecyclerView(this) }

    protected val headerLogoView by lazy { AppCompatImageView(this) }

    abstract val copyrightIcon: Int

    abstract val copyrightUrl: String

    override val contentView: View
        get() {
            return recyclerView
        }

    override val headerView: View
        get() {
            return headerLogoView
        }

    protected val dataList = ArrayList<D>()

    protected val adapter by lazy {
        ItemAdapter(dataList, ::onCreateHolder, ::onBindHolder, ::getHolderType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headerLogoView.setImageResource(copyrightIcon)
        if (copyrightUrl.isNotEmpty()) {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(copyrightUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
        bindContent()
    }

    protected open fun bindContent() {
        recyclerView.layoutManager = getContentLayoutManager()
        recyclerView.adapter = adapter
    }

    protected abstract fun onCreateHolder(parent: ViewGroup, itemType: Int): H

    protected open fun getHolderType(data: D): Int {
        return 0
    }

    protected abstract fun onBindHolder(holder: H, data: D)

    protected abstract fun getContentLayoutManager(): LayoutManager

    protected class ItemAdapter<D : Any, H : ViewHolder>(
        private val data: List<D>,
        private val holderCreator: HolderCreator<H>,
        private val holderBinding: HolderBinding<H, D>,
        private val holderTypeProvider: HolderTypeProvider<D>
    ) : RecyclerView.Adapter<H>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
            return holderCreator.onCreate(parent, viewType)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: H, position: Int) {
            holderBinding.onBind(holder, data[position])
        }

        override fun getItemViewType(position: Int): Int {
            return holderTypeProvider.getHolderType(data[position])
        }

    }

    protected fun interface HolderCreator<H : ViewHolder> {
        fun onCreate(parent: ViewGroup, itemType: Int): H
    }

    protected fun interface HolderTypeProvider<D : Any> {
        fun getHolderType(data: D): Int
    }

    protected fun interface HolderBinding<H : ViewHolder, D : Any> {
        fun onBind(holder: H, data: D)
    }

}