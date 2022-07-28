package com.lollipop.pigment

interface PigmentProvider {

    val pigmentProviderHelper: PigmentProviderHelper

    fun registerPigment(page: PigmentPage) {
        pigmentProviderHelper.registerPigment(page)
    }

    fun unregisterPigment(page: PigmentPage) {
        pigmentProviderHelper.unregisterPigment(page)
    }

}