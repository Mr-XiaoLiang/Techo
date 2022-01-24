package com.lollipop.bigboom.explosive

import com.lollipop.base.util.doAsync
import com.lollipop.base.util.onUI
import com.lollipop.bigboom.Basket
import com.lollipop.bigboom.Explosive
import java.util.*
import kotlin.collections.ArrayList

/**
 * 字符串的按照字拆解的大爆炸
 */
class CharExplosive : Explosive {

    private val stringList = LinkedList<String>()

    private val patchesList = ArrayList<String>()

    private var isSrcChanged = true

    private fun putValue(value: String) {
        isSrcChanged = true
        stringList.add(value)
    }

    override fun pilot(basket: Basket) {
        if (!isSrcChanged) {
            onUI {
                basket.goodPatches(patchesList.toTypedArray())
            }
            return
        }
        val values = LinkedList(stringList)
        doAsync({
            onUI {
                basket.failedDetonate(-1, it.localizedMessage ?: "", it)
            }
        }) {
            val resultList = ArrayList<String>()
            while (values.isNotEmpty()) {
                val first = values.removeFirst()
                if (first.isNotEmpty()) {
                    val charArray = first.toCharArray()
                    charArray.forEach {
                        resultList.add(String(charArrayOf(it)))
                    }
                }
            }
            patchesList.clear()
            patchesList.addAll(resultList)
            isSrcChanged = false
            onUI {
                basket.goodPatches(resultList.toTypedArray())
            }
        }
    }

    override fun putFuel(any: Any) {
        if (any is String) {
            putValue(any)
        } else {
            putValue(any.toString())
        }
    }

    override fun clear() {
        stringList.clear()
        patchesList.clear()
        isSrcChanged = true
    }

}