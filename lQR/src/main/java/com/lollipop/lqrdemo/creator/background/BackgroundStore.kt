package com.lollipop.lqrdemo.creator.background

object BackgroundStore {

    private val store = HashMap<String, BackgroundInfo>()

    var currentType: String = ""
        private set

    fun set(info: BackgroundInfo, select: Boolean = true) {
        val clazz = info.javaClass
        store[getTypeName(clazz)] = info
        if (select) {
            selectBackgroundType(clazz)
        }
    }

    fun get(type: String = currentType): BackgroundInfo? {
        return store[type]
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

}