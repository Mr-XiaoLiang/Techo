package com.lollipop.pigment

class PigmentProviderHelper : PigmentPage, PigmentProvider {

    private val pageList = ArrayList<PigmentPage>()

    override fun onDecorationChanged(pigment: Pigment) {
        pageList.forEach { it.onDecorationChanged(pigment) }
    }

    override fun registerPigment(page: PigmentPage) {
        pageList.add(page)
    }

    override fun unregisterPigment(page: PigmentPage) {
        pageList.remove(page)
    }
}