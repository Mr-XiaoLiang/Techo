package com.lollipop.fragment

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
class FragmentPager {

    fun setAdapter(viewPager2: ViewPager2) {
        // TODO
//        viewPager2.adapter
    }

    private class PageAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        private val creator: FragmentCreator,
        private val itemCountProvider: () -> Int,
        private val infoProvider: (Int) -> FragmentInfo?
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        init {
            setHasStableIds(true)
        }

        override fun getItemCount(): Int {
            return itemCountProvider()
        }

        override fun createFragment(position: Int): Fragment {
            val info = infoProvider(position) ?: throw IllegalArgumentException(
                "FragmentInfo 找不到了，count = ${itemCount}, position = $position"
            )
            return creator.create(info, null)
        }

        override fun getItemId(position: Int): Long {
            if (position < 0 || position >= itemCount) {
                return RecyclerView.NO_ID
            }
            val info = infoProvider(position) ?: return RecyclerView.NO_ID
            return ItemFlag.makeFlag(System.identityHashCode(info.fragment), position)
        }

        override fun containsItem(itemId: Long): Boolean {
            val hash = ItemFlag.getHash(itemId)
            val position = ItemFlag.getPosition(itemId)
            if (position < 0 || position >= itemCount) {
                return false
            }
            val info = infoProvider(position) ?: return false
            return System.identityHashCode(info.fragment) == hash
        }

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