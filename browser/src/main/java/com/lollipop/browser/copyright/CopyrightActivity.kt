package com.lollipop.browser.copyright

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lollipop.base.util.onClick
import com.lollipop.browser.R
import com.lollipop.browser.base.HeaderActivity

abstract class CopyrightActivity<H : ViewHolder, D : Any> : HeaderActivity() {

    companion object {
        private const val LOGO_SIZE = 80F
    }

    protected val recyclerView by lazy { RecyclerView(this) }

    abstract val copyrightLogo: Int
    abstract val copyrightLogoBackgroundColor: Int
    abstract val copyrightUrl: String
    abstract val copyrightTitle: Int
    abstract val copyrightInfo: Int

    override val contentView: View
        get() {
            return recyclerView
        }

    protected val dataList = ArrayList<D>()

    protected val adapter by lazy {
        ItemAdapter(dataList, ::onCreateHolder, ::onBindHolder, ::getHolderType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(copyrightTitle)
        setOptionButton {
            it.setImageResource(R.drawable.ic_baseline_info_24)
            it.onClick {
                showCopyrightInfo()
            }
        }
        bindContent()
    }

    private fun showCopyrightInfo() {
        MaterialAlertDialogBuilder(this)
            .setMessage(copyrightInfo)
            .setPositiveButton(R.string.copyright_info_button) { dialog, _ ->
                openCopyrightHome()
                dialog.dismiss()
            }
            .show()
    }

    override fun createHeaderView(): View {
        val logoSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, LOGO_SIZE, resources.displayMetrics
        ).toInt()
        val headerView = FrameLayout(this)
        val headerLogoView = AppCompatImageView(this)
        headerView.addView(headerLogoView,
            FrameLayout.LayoutParams(logoSize, logoSize).apply {
                gravity = Gravity.CENTER
            }
        )
        headerLogoView.setImageResource(copyrightLogo)
        headerView.setBackgroundColor(copyrightLogoBackgroundColor)

        onTitleClick {
            openCopyrightHome()
        }

        return headerView
    }

    private fun openCopyrightHome() {
        if (copyrightUrl.isNotEmpty()) {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(copyrightUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    protected fun resetData(list: List<D>) {
        dataList.clear()
        dataList.addAll(list)
        adapter.notifyDataSetChanged()
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