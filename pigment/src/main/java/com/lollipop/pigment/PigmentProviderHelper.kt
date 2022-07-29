package com.lollipop.pigment

class PigmentProviderHelper : PigmentPage {

    private val pageList = ArrayList<PigmentPage>()

    override fun onDecorationChanged(pigment: Pigment) {
        pageList.forEach { it.onDecorationChanged(pigment) }
    }

    fun registerPigment(page: PigmentPage) {
        pageList.add(page)
    }

    fun unregisterPigment(page: PigmentPage) {
        pageList.remove(page)
    }
}