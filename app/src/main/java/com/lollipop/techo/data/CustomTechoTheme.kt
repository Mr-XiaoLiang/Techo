package com.lollipop.techo.data

import android.content.Context
import android.graphics.Color
import org.json.JSONObject
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

object CustomTechoTheme {

    private const val K_KEY = "key"
    private const val K_BASE = "base"
    private const val K_PRIMARY_COLOR = "primaryColor"
    private const val K_SECONDARY_COLOR = "secondaryColor"
    private const val K_PRIMARY_VARIANT = "primaryVariant"
    private const val K_ON_PRIMARY_TITLE = "onPrimaryTitle"
    private const val K_ON_PRIMARY_BODY = "onPrimaryBody"
    private const val K_SECONDARY_VARIANT = "secondaryVariant"
    private const val K_ON_SECONDARY_TITLE = "onSecondaryTitle"
    private const val K_ON_SECONDARY_BODY = "onSecondaryBody"
    private const val K_BACKGROUND_COLOR = "backgroundColor"
    private const val K_ON_BACKGROUND_TITLE = "onBackgroundTitle"
    private const val K_ON_BACKGROUND_BODY = "onBackgroundBody"
    private const val K_EXTREME = "extreme"
    private const val K_EXTREME_REVERSAL = "extremeReversal"
    private const val K_ON_EXTREME_TITLE = "onExtremeTitle"
    private const val K_ON_EXTREME_BODY = "onExtremeBody"

    private const val DIR_PROFILE = "theme"

    private fun getProfileDir(context: Context): File {
        return File(context.filesDir, DIR_PROFILE)
    }

    private fun getProfileList(context: Context): Array<out File> {
        val profileDir = getProfileDir(context)
        return profileDir.listFiles() ?: emptyArray()
    }

    fun loadProfiles(context: Context, adapter: (File) -> Unit) {
        try {
            val list = getProfileList(context)
            list.forEach {
                try {
                    adapter(it)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /*
    {
        "key": "",
        "base": "light/dark",
        "primaryColor": "#FFFFFFFF",
        "secondaryColor": "#FFFFFFFF",
        "primaryVariant": "#FFFFFFFF",
        "onPrimaryTitle": "#FFFFFFFF",
        "onPrimaryBody": "#FFFFFFFF",
        "secondaryVariant": "#FFFFFFFF",
        "onSecondaryTitle": "#FFFFFFFF",
        "onSecondaryBody": "#FFFFFFFF",
        "backgroundColor": "#FFFFFFFF",
        "onBackgroundTitle": "#FFFFFFFF",
        "onBackgroundBody": "#FFFFFFFF",
        "extreme": "#FFFFFFFF",
        "extremeReversal": "#FFFFFFFF",
        "onExtremeTitle": "#FFFFFFFF",
        "onExtremeBody": "#FFFFFFFF"
    }
    */
    fun parse(json: String, file: File?): Result<TechoTheme.Custom> {
        try {
            val obj = JSONObject(json)
            val key = obj.optString(K_KEY)
            if (key.isEmpty() || key.isBlank()) {
                return Result.failure(ProfileKeyNotFoundException())
            }
            val baseName = obj.optString(K_BASE)
            val baseTheme = TechoTheme.findBase(baseName) ?: TechoTheme.DEFAULT
            val colorKeys = obj.keys()
            val map = HashMap<String, Int>()
            for (k in colorKeys) {
                if (k == K_KEY || k == K_BASE) {
                    continue
                }
                val colorValue = obj.optString(k)
                if (colorValue.isNotEmpty()) {
                    val color = parseColorOrNull(colorValue)
                    if (color != null) {
                        map[k] = color
                    }
                }
            }
            return Result.success(TechoTheme.Custom(key, ThemeProfileByJson(baseTheme, map), file))
        } catch (e: Throwable) {
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    private fun parseColorOrNull(value: String): Int? {
        try {
            return Color.parseColor(value)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    class ProfileKeyNotFoundException : IllegalArgumentException()

    private class ThemeProfileByJson(
        override val base: TechoTheme.Base,
        private val map: Map<String, Int>
    ) : TechoTheme.Custom.Profile {

        class SimpleProperty(
            private val map: Map<String, Int>
        ) : ReadOnlyProperty<ThemeProfileByJson, Int?> {

            private var isInitialized = false
            private var contentValue: Int? = null

            override fun getValue(thisRef: ThemeProfileByJson, property: KProperty<*>): Int? {
                synchronized(this) {
                    if (!isInitialized) {
                        val name = property.name
                        if (map.containsKey(name)) {
                            contentValue = map[name]
                        }
                        isInitialized = true
                    }
                }
                return contentValue
            }

        }

        private fun fromName(): ReadOnlyProperty<ThemeProfileByJson, Int?> {
            return SimpleProperty(map)
        }

        override val primaryColor: Int? by fromName()
        override val secondaryColor: Int? by fromName()
        override val primaryVariant: Int? by fromName()
        override val onPrimaryTitle: Int? by fromName()
        override val onPrimaryBody: Int? by fromName()
        override val secondaryVariant: Int? by fromName()
        override val onSecondaryTitle: Int? by fromName()
        override val onSecondaryBody: Int? by fromName()
        override val backgroundColor: Int? by fromName()
        override val onBackgroundTitle: Int? by fromName()
        override val onBackgroundBody: Int? by fromName()
        override val extreme: Int? by fromName()
        override val extremeReversal: Int? by fromName()
        override val onExtremeTitle: Int? by fromName()
        override val onExtremeBody: Int? by fromName()

    }

}