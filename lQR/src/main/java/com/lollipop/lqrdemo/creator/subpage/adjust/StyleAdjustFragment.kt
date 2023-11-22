package com.lollipop.lqrdemo.creator.subpage.adjust

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.creator.writer.QrWriterLayer
import com.lollipop.lqrdemo.databinding.FragmentStyleAdjustBinding
import com.lollipop.lqrdemo.databinding.ItemQrPositionDetectionTabBinding

open class StyleAdjustFragment : BaseFragment(), StyleAdjustContentFragment.Callback {

    private val binding: FragmentStyleAdjustBinding by lazyBind()

    private val tabList = ArrayList<TabInfo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    protected fun setContentTabs(tabs: List<TabInfo>) {
        tabList.clear()
        tabList.addAll(tabs)
        notifyTabSetChanged()
    }

    protected fun notifyTabSetChanged() {
        TODO()
    }

    class TabInfo(
        val tabIcon: Int,
        val panel: Class<out StyleAdjustContentFragment>,
        val key: String
    )

    override fun notifyContentChanged() {
        TODO("Not yet implemented")
    }

    override fun notifyLayerChanged(layer: Class<out QrWriterLayer>) {
        TODO("Not yet implemented")
    }

    private class TabHolder(
        val binding: ItemQrPositionDetectionTabBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(info: TabInfo, isSelected: Boolean) {
            binding.tabIconClip.isStrokeEnable = isSelected
            Glide.with(binding.tabIconView).load(info.tabIcon).into(binding.tabIconView)
        }

    }

}