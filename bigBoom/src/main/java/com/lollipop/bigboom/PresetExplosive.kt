package com.lollipop.bigboom

import com.lollipop.bigboom.explosive.CharExplosive


enum class PresetExplosive(val clazz: Class<out Explosive>) {

    CHAR(CharExplosive::class.java)

}