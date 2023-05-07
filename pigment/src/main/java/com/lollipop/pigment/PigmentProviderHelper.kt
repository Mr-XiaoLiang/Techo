package com.lollipop.pigment

import java.lang.ref.WeakReference

class PigmentProviderHelper : PigmentPage {

    private val pageList = ArrayList<WeakReference<PigmentPage>>()

    override fun onDecorationChanged(pigment: Pigment) {
        pageList.forEach { it.get()?.onDecorationChanged(pigment) }
    }

    fun registerPigment(page: PigmentPage) {
        val last = pageList.find { it.get() === page }
        if (last != null) {
            return
        }
        pageList.add(WeakReference(page))
    }

    fun unregisterPigment(page: PigmentPage) {
        pageList.removeIf {
            val p = it.get()
            p == null || p === page
        }
    }
}