package com.lollipop.fragment

import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

object FragmentHelper {

    fun with(fragmentManager: FragmentManager, lifecycle: Lifecycle): Builder {
        return Builder(fragmentManager, lifecycle)
    }

    fun with(activity: AppCompatActivity): Builder {
        return with(activity.supportFragmentManager, activity.lifecycle)
    }

    fun with(fragment: Fragment): Builder {
        return with(fragment.childFragmentManager, fragment.lifecycle)
    }

    class Builder internal constructor(
        private val fragmentManager: FragmentManager,
        private val lifecycle: Lifecycle
    ) {

        private val infoList = ArrayList<FragmentInfo>()

        fun addInfo(info: FragmentInfo): Builder {
            infoList.add(info)
            return this
        }

        fun addInfo(info: List<FragmentInfo>): Builder {
            infoList.addAll(info)
            return this
        }

        fun addInfo(clazz: Class<Fragment>, tag: String): Builder {
            return addInfo(SimpleFragmentInfo(clazz, tag))
        }

        fun into(container: FrameLayout): FragmentSwitcher {
            val switcher = FragmentSwitcher(fragmentManager, container.id)
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                switcher.addAll(infoList)
                lifecycle.addObserver(switcher)
            }
            return switcher
        }

        fun into(viewPager2: ViewPager2): FragmentPager {
            val fragmentPager = FragmentPager.create(fragmentManager, lifecycle)
            viewPager2.adapter = fragmentPager
            fragmentPager.infoList.addAll(infoList)
            return fragmentPager
        }

        fun into(recyclerView: RecyclerView): FragmentPager {
            val fragmentPager = FragmentPager.create(fragmentManager, lifecycle)
            recyclerView.adapter = fragmentPager
            fragmentPager.infoList.addAll(infoList)
            return fragmentPager
        }

    }

}