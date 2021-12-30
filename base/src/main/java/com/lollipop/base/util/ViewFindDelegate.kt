package com.lollipop.base.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

/**
 * @author lollipop
 * @date 2021/12/30 21:57
 */
class ViewFindDelegate<C : Any, T : View>(private val target: Any?) {

    companion object {
        private val idMap = HashMap<String, Int>()
    }

    /**
     * 上次寻找的View的Id名称
     * 用于匹配寻找的对象，减少不必要的开支
     */
    private var lastViewName = ""

    /**
     * 上次寻找的View的Id
     * 用于加速寻找的过程
     */
    private var lastViewId = 0

    /**
     * 上次寻找到的View
     * 弱引用可以减少不必要的泄漏问题
     */
    private var lastFoundView = WeakReference<T>(null)

    private fun findId(name: String, context: Context): Int {
        // 一级缓存，保留最近的一次View查找记录，便于快速返回
        // 一般情况下，一个View的委托是一个实例，因此表示第一次查找后，可以一直使用此id
        if (lastViewName == name) {
            return lastViewId
        }
        // 二级缓存，全局的id名次与值的对应表
        // 只有使用过之后就可以快速的查询到，避免列表中的同一个id反复查找
        var id = idMap[name]
        // 当缓存中完全找不到时，此处通过res进行查找
        if (id == null) {
            id = context.resources.getIdentifier(
                name,
                "id",
                context.packageName
            )
        }
        // 对查找到的数据进行一级与二级缓存
        lastViewName = name
        lastViewId = id
        idMap[name] = id
        return id
    }

    operator fun getValue(any: C, property: KProperty<*>): T? {
        // 如果已经寻找过了这个View，认为上次寻找的就是这个View，那么我们直接使用
        if (lastViewName == property.name) {
            val lastView = lastFoundView.get()
            if (lastView != null) {
                return lastView
            }
        }
        // 如果结果为null，那么我们尝试重新找
        val group = target ?: any
        val viewName = property.name
        val foundView: T? = when (group) {
            is View -> {
                group.findViewById(findId(viewName, group.context))
            }
            is Fragment -> {
                group.context?.let {
                    group.view?.findViewById(findId(viewName, it))
                }
            }
            is Activity -> {
                group.findViewById(findId(viewName, group))
            }
            is RecyclerView.ViewHolder -> {
                group.itemView.findViewById(findId(viewName, group.itemView.context))
            }
            is Dialog -> {
                group.findViewById(findId(viewName, group.context))
            }
            is Window -> {
                group.findViewById(findId(viewName, group.context))
            }
            else -> {
                null
            }
        }
        lastFoundView = WeakReference(foundView)
        return foundView
    }
}

inline fun <reified C : Any, reified T : View> C.findInSelf() = ViewFindDelegate<C, T>(null)

inline fun <reified C : Any, reified T : View> findIn(target: Any) = ViewFindDelegate<C, T>(target)
