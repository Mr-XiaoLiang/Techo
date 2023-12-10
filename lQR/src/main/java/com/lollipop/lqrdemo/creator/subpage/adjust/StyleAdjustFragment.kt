package com.lollipop.lqrdemo.creator.subpage.adjust

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lollipop.base.util.bind
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.fragment.FragmentInfo
import com.lollipop.fragment.FragmentPager
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.databinding.FragmentStyleAdjustBinding
import com.lollipop.lqrdemo.databinding.ItemQrPositionDetectionTabBinding

abstract class StyleAdjustFragment : BaseFragment(), StyleAdjustContentFragment.Callback {

    private val binding: FragmentStyleAdjustBinding by lazyBind()

    private val tabList = ArrayList<TabInfo>()

    private val fragmentHelper by lazy {
        FragmentPager.create(childFragmentManager, this.lifecycle)
    }

    private val tabAdapter = TabAdapter(tabList, ::isTabSelected, ::onTabClick)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentHelper.bind(binding.styleAdjustGroup)
        binding.styleAdjustGroup.isUserInputEnabled = false
        binding.styleTabGroup.adapter = tabAdapter
        binding.styleTabGroup.layoutManager = LinearLayoutManager(
            view.context,
            RecyclerView.HORIZONTAL,
            false
        )
    }

    private fun isTabSelected(tabInfo: TabInfo): Boolean {
        val currentItem = binding.styleAdjustGroup.currentItem
        if (currentItem < 0 || currentItem >= tabList.size) {
            return false
        }
        val currentInfo = tabList[currentItem]
        return currentInfo === tabInfo || currentInfo.key == tabInfo.key
    }

    private fun onTabClick(position: Int, info: TabInfo) {
        if (position < 0 || position >= tabList.size) {
            return
        }
        val currentInfo = tabList[position]
        if (currentInfo.key == info.key) {
            binding.styleAdjustGroup.currentItem = position
        }
    }

    protected fun setContentTabs(tabs: List<TabInfo>) {
        tabList.clear()
        tabList.addAll(tabs)
        notifyTabSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    protected fun notifyTabSetChanged() {
        fragmentHelper.reset(tabList)
        tabAdapter.notifyDataSetChanged()
    }

    class TabInfo(
        val tabIcon: Int,
        val panel: Class<out StyleAdjustContentFragment>,
        val key: String
    ) : FragmentInfo {
        override val fragment: Class<out Fragment> = panel
        override val tag: String = key
    }

    private class TabAdapter(
        private val list: List<TabInfo>,
        private val isSelected: (TabInfo) -> Boolean,
        private val onTabClick: (Int, TabInfo) -> Unit
    ) : RecyclerView.Adapter<TabHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabHolder {
            return TabHolder(parent.bind(false), ::onItemClick)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: TabHolder, position: Int) {
            val info = list[position]
            holder.bind(info, isSelected(info))
        }

        private fun onItemClick(position: Int) {
            if (position < 0 || position >= list.size) {
                return
            }
            onTabClick(position, list[position])
        }

    }

    private class TabHolder(
        val binding: ItemQrPositionDetectionTabBinding,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.onClick {
                onItemClick()
            }
        }

        private fun onItemClick() {
            onItemClick(adapterPosition)
        }

        fun bind(info: TabInfo, isSelected: Boolean) {
            binding.tabIconClip.isStrokeEnable = isSelected
            Glide.with(binding.tabIconView).load(info.tabIcon).into(binding.tabIconView)
        }

    }

}