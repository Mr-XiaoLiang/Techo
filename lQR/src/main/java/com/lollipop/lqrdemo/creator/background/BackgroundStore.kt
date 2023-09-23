package com.lollipop.lqrdemo.creator.background

import androidx.lifecycle.MutableLiveData
import com.lollipop.base.util.ListenerManager

object BackgroundStore {

    private val store = HashMap<String, BackgroundInfo>()

    private val listenerManager = ListenerManager<OnBackgroundChangedListener>()

    private val cornerInfo = MutableLiveData<BackgroundCorner>()

    var currentType: String = ""
        private set

    fun getCorner(): BackgroundCorner? {
        return cornerInfo.value
    }

    fun setCorner(c: BackgroundCorner) {
        cornerInfo.value = c
    }

    fun set(info: BackgroundInfo, select: Boolean = true) {
        val clazz = info.javaClass
        store[getTypeName(clazz)] = info
        if (select) {
            selectBackgroundType(clazz)
        }
    }

    fun get(type: String = currentType): BackgroundInfo {
        return store[type] ?: BackgroundInfo.None
    }

    inline fun <reified T : BackgroundInfo> getByType(): T? {
        try {
            val clazz = T::class.java
            val info = get(getTypeName(clazz))
            if (info is T) {
                return info
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    fun selectBackgroundType(clazz: Class<out BackgroundInfo>) {
        currentType = getTypeName(clazz)
    }

    inline fun <reified T : BackgroundInfo> selectBackgroundType() {
        selectBackgroundType(T::class.java)
    }

    fun getTypeName(clazz: Class<out BackgroundInfo>): String {
        return clazz.name
    }

    fun addListener(listener: OnBackgroundChangedListener) {
        this.listenerManager.addListener(listener)
    }

    fun removeListener(listener: OnBackgroundChangedListener) {
        this.listenerManager.removeListener(listener)
    }

    fun interface OnBackgroundChangedListener {
        fun onBackgroundChanged()
    }

}