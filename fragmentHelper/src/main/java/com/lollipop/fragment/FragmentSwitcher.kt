package com.lollipop.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.lollipop.base.util.ListenerManager

/**
 * fragment的切换器
 */
class FragmentSwitcher(
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) {

    private val creator = FragmentCreator()
    private val changedCallback = ListenerManager<FragmentChangedCallback>()
    private val infoMap = HashMap<String, FragmentInfo>()
    private val cache = HashMap<String, Fragment>()

    var currentFragment = ""
        private set

    private val commonFragmentTag by lazy {
        getFragmentTag("")
    }

    fun addAll(list: List<FragmentInfo>) {
        list.forEach {
            infoMap[it.tag] = it
        }
    }

    fun notifyDataSetChanged(tag: String = "") {
        switchTo(tag, null)
    }

    fun switchTo(tag: String, arguments: Bundle?): Int {
        val transaction = fragmentManager.beginTransaction()
        // 隐藏老的Fragment
        val prefix = commonFragmentTag
        fragmentManager.fragments.forEach {
            // 通过前缀把所有前面的Fragment都隐藏了，如果通过tag隐藏某个Fragment，
            // 可能会在Fragment的恢复时，出现遗漏
            if (it.tag?.startsWith(prefix) == true) {
                transaction.hide(it)
                transaction.setMaxLifecycle(it, Lifecycle.State.STARTED)
            }
        }
        val newInfo = infoMap[tag]
        // 如果能找到新的Fragment，那么就让它显示出来
        if (newInfo != null) {
            val newTag = getFragmentTag(tag)
            var oldFragment = fragmentManager.findFragmentByTag(newTag)
            if (oldFragment == null) {
                oldFragment = cache[tag]
            }
            if (oldFragment != null) {
                changeArguments(oldFragment, arguments)
                transaction.show(oldFragment)
                transaction.setMaxLifecycle(oldFragment, Lifecycle.State.RESUMED)
            } else {
                val newFragment = creator.create(newInfo, arguments)
                changeArguments(newFragment, arguments)
                transaction.add(containerId, newFragment, newTag)
                transaction.setMaxLifecycle(newFragment, Lifecycle.State.RESUMED)
                cache[tag] = newFragment
            }
        }
        currentFragment = tag
        return transaction.commit()
    }

    private fun changeArguments(fragment: Fragment, arguments: Bundle?) {
        val arg = fragment.arguments ?: Bundle()
        if (arguments != null) {
            arg.putAll(arguments)
        }
        fragment.arguments = arg
        if (fragment is LollipopFragment) {
            fragment.onArgumentsChanged()
        }
        changedCallback.invoke { it.onFragmentChanged(fragment) }
    }

    private fun getFragmentTag(tag: String): String {
        return "LollipopSwitcher-$containerId-$tag"
    }

    fun addFragmentCreatedCallback(callback: FragmentCreatedCallback) {
        creator.addFragmentCreatedCallback(callback)
    }

    fun removeFragmentCreatedCallback(callback: FragmentCreatedCallback) {
        creator.removeFragmentCreatedCallback(callback)
    }

    fun addFragmentChangedCallback(callback: FragmentChangedCallback) {
        changedCallback.addListener(callback)
    }

    fun removeFragmentChangedCallback(callback: FragmentChangedCallback) {
        changedCallback.removeListener(callback)
    }

}