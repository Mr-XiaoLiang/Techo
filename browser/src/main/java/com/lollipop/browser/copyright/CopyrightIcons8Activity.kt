package com.lollipop.browser.copyright

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.browser.R
import com.lollipop.browser.databinding.ItemCopyrightIcons8Binding

class CopyrightIcons8Activity : CopyrightActivity<CopyrightIcons8Activity.Icons8Holder, Int>() {

    companion object {
        private val iconsList = arrayListOf(
            R.drawable.ic_baidu,
            R.drawable.ic_bing,
            R.drawable.ic_google
        )
    }

    override val copyrightLogo: Int = R.drawable.ic_icons8
    override val copyrightLogoBackgroundColor: Int = 0xFF54AD50.toInt()
    override val copyrightUrl: String = "https://icons8.com/"
    override val copyrightTitle: Int = R.string.copyright_title_icons8
    override val copyrightInfo: Int = R.string.copyright_info_icons8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetData(iconsList)
    }

    override fun onCreateHolder(parent: ViewGroup, itemType: Int): Icons8Holder {
        return Icons8Holder(parent.bind(false))
    }

    override fun getContentLayoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(this, 4, RecyclerView.VERTICAL, false)
    }

    override fun onBindHolder(holder: Icons8Holder, data: Int) {
        holder.bind(data)
    }

    class Icons8Holder(
        private val binding: ItemCopyrightIcons8Binding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(icon: Int) {
            binding.iconView.setImageResource(icon)
        }

    }

}