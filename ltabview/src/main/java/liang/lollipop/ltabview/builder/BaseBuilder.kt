package liang.lollipop.ltabview.builder

import androidx.viewpager.widget.ViewPager
import liang.lollipop.ltabview.LTabView

/**
 * @date: 2019-04-19 23:56
 * @author: lollipop
 * 基础构造器
 */
abstract class BaseBuilder(protected val tabView: LTabView) {

    private val onSelectedListenerList = ArrayList<LTabView.OnSelectedListener>()

    init {
        tabView.onSelectedListener = SimpleSelectedListener { i ->
            onSelectedListenerList.forEach { it.onTabSelected(i) }
        }
    }

    var animationDuration: Long
        get() = tabView.animationDuration
        set(value) {
            tabView.animationDuration = value
        }

    var layoutStyle: LTabView.Style
        get() = tabView.style
        set(value) {
            tabView.style = value
        }

    var isLimit: Boolean
        get() = tabView.isLimit
        set(value) {
            tabView.isLimit = value
        }

    var isUniform: Boolean
        get() = tabView.isUniform
        set(value) {
            tabView.isUniform = value
        }

    fun selected(index: Int) {
        tabView.selected(index)
    }

    fun onSelected(callback: (Int) -> Unit): LTabView.OnSelectedListener {
        val listener = SimpleSelectedListener(callback)
        onSelectedListenerList.add(listener)
        return listener
    }

    fun addOnSelectedListener(listener: LTabView.OnSelectedListener) {
        onSelectedListenerList.add(listener)
    }

    fun remmoveOnSelectedListener(listener: LTabView.OnSelectedListener) {
        onSelectedListenerList.remove(listener)
    }

    fun setupWithViewPager(viewPager: ViewPager) {
        viewPager.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener(){
            override fun onPageSelected(position: Int) {
                tabView.selected(position)
            }
        })
        onSelected {
            viewPager.currentItem = it
        }
    }

    private class SimpleSelectedListener(private val callback: (Int) -> Unit): LTabView.OnSelectedListener {
        override fun onTabSelected(index: Int) {
            callback(index)
        }
    }

}