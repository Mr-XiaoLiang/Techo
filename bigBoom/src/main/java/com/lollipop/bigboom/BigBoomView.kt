package com.lollipop.bigboom

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author lollipop
 * @date 2022/1/7 21:59
 * 大爆炸的View
 */
class BigBoomView(
    context: Context, attr: AttributeSet?, style: Int
) : ViewGroup(context, attr, style) {

    private val patchList = ArrayList<String>()
    private val selectedPatchSet = TreeSet<Int>()

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        TODO("Not yet implemented")
    }

    fun setPatches(value: Array<String>, defaultSelected: Array<Int> = emptyArray()) {
        patchList.clear()
        patchList.addAll(value)
        selectedPatchSet.clear()
        selectedPatchSet.addAll(defaultSelected)
        notifyPatchesChanged()
    }

    private fun notifyPatchesChanged() {
        TODO("Not yet implemented")
    }

}