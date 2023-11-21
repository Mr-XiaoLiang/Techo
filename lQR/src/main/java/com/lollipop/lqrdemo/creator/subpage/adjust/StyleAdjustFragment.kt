package com.lollipop.lqrdemo.creator.subpage.adjust

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.databinding.FragmentStyleAdjustBinding

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
        notifyContentSetChanged()
    }

    protected fun notifyContentSetChanged() {
        TODO()
    }

    class TabInfo(
        val tabIcon: Int,
        val panel: Class<out StyleAdjustContentFragment>
    )

    override fun notifyContentChanged() {
        TODO("Not yet implemented")
    }

}