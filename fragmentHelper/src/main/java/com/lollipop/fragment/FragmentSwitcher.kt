package com.lollipop.fragment

import android.os.Bundle
import androidx.annotation.AnimatorRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lollipop.base.util.ListenerManager

/**
 * fragment的切换器
 */
class FragmentSwitcher internal constructor(
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) : LifecycleEventObserver {

    companion object {
        private val EMPTY_TRANSACTION_ANIMATION = TransactionAnimation(0, 0, 0, 0)
    }

    private val creator = FragmentCreator()
    private val changedCallback = ListenerManager<FragmentChangedCallback>()
    private val infoMap = HashMap<String, FragmentInfo>()
    private val cache = HashMap<String, Fragment>()

    /**
     * 当前的Fragment的Tag
     * 它与FragmentInfo中的tag保持一致
     */
    var currentFragment = ""
        private set

    val size: Int
        get() {
            return infoMap.size
        }

    private val commonFragmentTag by lazy {
        getFragmentTag("")
    }

    /**
     * 添加一个FragmentInfo
     * 它不会清理以前的FragmentInfo
     * 但是它会覆盖tag相同的FragmentInfo
     * 因此，为了避免不必要的问题，请保证FragmentInfo中的tag非必要不会重复
     */
    fun add(info: FragmentInfo) {
        infoMap[info.tag] = info
    }

    /**
     * 添加一组FragmentInfo
     * 它不会清理以前的FragmentInfo
     * 但是它会覆盖tag相同的FragmentInfo
     * 因此，为了避免不必要的问题，请保证FragmentInfo中的tag非必要不会重复
     */
    fun addAll(list: List<FragmentInfo>) {
        list.forEach {
            add(it)
        }
    }

    /**
     * 通知数据发生了变化
     * 它不会清理数据垃圾
     * 因此，如果数据发生了巨大的变化，建议使用顺序为：
     * clear -> addAll -> notifyDataSetChanged
     * @param tag 需要切换至的Fragment，它默认是currentFragment，但是当clear之后，它的内容将会被清空
     */
    fun notifyDataSetChanged(tag: String = currentFragment) {
        switchTo(tag, null)
    }

    /**
     * 清理切换器中的数据缓存，以及FragmentManager中的由我们自己创建的Fragment
     */
    fun clear(): Int {
        currentFragment = ""
        infoMap.clear()
        cache.clear()
        val transaction = fragmentManager.beginTransaction()
        val prefix = commonFragmentTag
        fragmentManager.fragments.forEach {
            // 通过前缀把所有前面的Fragment都隐藏了，如果通过tag隐藏某个Fragment，
            // 可能会在Fragment的恢复时，出现遗漏
            if (it.tag?.startsWith(prefix) == true) {
                transaction.remove(it)
            }
        }
        return transaction.commit()
    }

    fun findByTag(tag: String): Fragment? {
        return fragmentManager.findFragmentByTag(getFragmentTag(tag))
    }

    inline fun <reified T : Fragment> findTypedByTag(tag: String): T? {
        val fragment = findByTag(tag)
        if (fragment is T) {
            return fragment
        }
        return null
    }

    /**
     * 切换至指定的Fragment
     * @param tag 需要显示的Fragment的Tag，它需要存在于Switcher的数据集合中
     * @param arguments 被切换出的Fragment的新的参数内容
     * @param animation 本次切换的动画指定
     */
    fun switchTo(
        tag: String,
        arguments: Bundle? = null,
        animation: TransactionAnimation = EMPTY_TRANSACTION_ANIMATION
    ): Int {
        val transaction = fragmentManager.beginTransaction()
        transaction.setReorderingAllowed(true)
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
                // 如果找到的老得Fragment类型和现在的不一样，那么我们仍然需要重新创建
                // 这种场景出现于Tag一致，但是Fragment的Class不一致的场景
                if (oldFragment::class.java === newInfo.fragment) {
                    // 如果类型一致，那么咱们把它显示出来
                    changeArguments(oldFragment, arguments)
                    transaction.show(oldFragment)
                    transaction.setMaxLifecycle(oldFragment, Lifecycle.State.RESUMED)
                } else {
                    // 如果类型不一致，那么从FragmentManager和cache中移除它
                    transaction.remove(oldFragment)
                    cache.remove(tag)
                    oldFragment = null
                }
            }
            // 如果没有老的Fragment，那么我们创建一个新的
            if (oldFragment == null) {
                val newFragment = creator.create(newInfo, arguments)
                changeArguments(newFragment, arguments)
                transaction.add(containerId, newFragment, newTag)
                transaction.setMaxLifecycle(newFragment, Lifecycle.State.RESUMED)
                cache[tag] = newFragment
            }
        }
        // 如果动画不为空，那么咱们设置本次事务的动画
        if (animation.isNotEmpty) {
            transaction.setCustomAnimations(
                animation.enter,
                animation.exit,
                animation.popEnter,
                animation.popExit
            )
        }
        // 绑定当前的Fragment的Tag
        currentFragment = tag
        return transaction.commit()
    }

    /**
     * 修改Fragment的参数
     * 它会涉及到两个回调函数
     * 需要先按照传入参数绑定数据
     * 然后依次触发外部的参数修改回调和Fragment自身的参数修改回调
     */
    private fun changeArguments(fragment: Fragment, arguments: Bundle?) {
        val arg = fragment.arguments ?: Bundle()
        if (arguments != null) {
            arg.putAll(arguments)
        }
        fragment.arguments = arg
        changedCallback.invoke { it.onFragmentChanged(fragment) }
        if (fragment is LollipopFragment) {
            fragment.onArgumentsChanged()
        }
    }

    /**
     * 创建一个专属于我们的Tag，它可以区分这个Fragment是否是我们创建的，
     * 并且是否是当前这个容器的Fragment
     */
    private fun getFragmentTag(tag: String): String {
        return "LollipopSwitcher-$containerId-$tag"
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

    /**
     * 添加一个Fragment参数发生了变更的回调函数
     */
    fun addFragmentChangedCallback(callback: FragmentChangedCallback) {
        changedCallback.addListener(callback)
    }

    /**
     * 移除一个Fragment参数发生了变更的回调函数
     */
    fun removeFragmentChangedCallback(callback: FragmentChangedCallback) {
        changedCallback.removeListener(callback)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (source.lifecycle.currentState <= Lifecycle.State.DESTROYED) {
            clear()
        }
    }

    /**
     * 事务动画的参数包装类
     */
    data class TransactionAnimation(
        @AnimatorRes
        val enter: Int,
        @AnimatorRes
        val exit: Int,
        @AnimatorRes
        val popEnter: Int = 0,
        @AnimatorRes
        val popExit: Int = 0
    ) {
        val isNotEmpty: Boolean = enter != 0 || exit != 0 || popEnter != 0 || popExit != 0
    }

}