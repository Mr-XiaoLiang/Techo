package com.lollipop.fragment

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

/**
 * fragment的切换器
 * 做ViewPager的包装
 * 本来想做一个ViewPager和ViewPager2都包含的
 * 但是想了下，抛弃ViewPager吧
 */
class FragmentPager private constructor(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    companion object {
        fun create(
            fragmentManager: FragmentManager,
            lifecycle: Lifecycle,
        ): FragmentPager {
            return FragmentPager(fragmentManager, lifecycle)
        }
    }

    private val creator = FragmentCreator()

    val infoList = ArrayList<FragmentInfo>()

//    init {
//        setHasStableIds(true)
//    }

    fun bind(viewPager2: ViewPager2) {
        viewPager2.adapter = this
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reset(list: List<FragmentInfo>) {
        infoList.clear()
        infoList.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return infoList.size
    }

    fun opt(position: Int): FragmentInfo? {
        if (position < 0 || position >= itemCount) {
            return null
        }
        return infoList[position]
    }

    override fun createFragment(position: Int): Fragment {
        val info = opt(position) ?: throw IllegalArgumentException(
            "FragmentInfo 找不到了，count = ${itemCount}, position = $position"
        )
        return creator.create(info, null)
    }

    override fun getItemId(position: Int): Long {
        if (position < 0 || position >= itemCount) {
            return RecyclerView.NO_ID
        }
        val info = opt(position) ?: return RecyclerView.NO_ID
        return ItemFlag.makeFlag(System.identityHashCode(info.fragment), position)
    }

    override fun containsItem(itemId: Long): Boolean {
        val hash = ItemFlag.getHash(itemId)
        val position = ItemFlag.getPosition(itemId)
        if (position < 0 || position >= itemCount) {
            return false
        }
        val info = opt(position) ?: return false
        return System.identityHashCode(info.fragment) == hash
    }

    /**
     * 添加一个Fragment创建事件的回调函数
     */
    fun addFragmentCreatedCallback(callback: FragmentCreatedCallback) {
        creator.addFragmentCreatedCallback(callback)
    }

    /**
     * 移除一个Fragment创建事件的回调函数
     */
    fun removeFragmentCreatedCallback(callback: FragmentCreatedCallback) {
        creator.removeFragmentCreatedCallback(callback)
    }

    private object ItemFlag {
        fun makeFlag(hash: Int, position: Int): Long {
            return hash.toULong().shl(32).or(position.toULong()).toLong()
        }

        fun getHash(flag: Long): Int {
            return flag.shr(32).toInt()
        }

        fun getPosition(flag: Long): Int {
            return flag.and(0xFFFFFF).toInt()
        }

    }

}

