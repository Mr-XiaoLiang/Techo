package com.lollipop.pigment

import java.lang.ref.WeakReference

class PigmentProviderHelper : PigmentPage {

    private val pageList = ArrayList<WeakReference<PigmentPage>>()

    private var lastPigment: Pigment? = null

    override fun onDecorationChanged(pigment: Pigment) {
        lastPigment = pigment
        pageList.forEach { it.get()?.onDecorationChanged(pigment) }
    }

    override val currentPigment: Pigment?
        get() = lastPigment

    fun registerPigment(page: PigmentPage) {
        val last = pageList.find { it.get() === page }
        if (last != null) {
            return
        }
        pageList.add(WeakReference(page))
        lastPigment?.let {
            page.onDecorationChanged(it)
        }
    }

    fun unregisterPigment(page: PigmentPage) {
        pageList.removeIf {
            val p = it.get()
            p == null || p === page
        }
    }
}