package com.lollipop.techo.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.reflect.KProperty

sealed class Preferences private constructor(
    private val sharedPreferences: SharedPreferences
) : SharedPreferences by sharedPreferences {

    companion object {
        fun ui(context: Context): UI {
            return UI(context)
        }

        fun default(context: Context): Default {
            return Default(context)
        }
    }

    constructor(context: Context, name: String) : this(
        context.getSharedPreferences(
            name,
            Context.MODE_PRIVATE
        )
    )

    class Default internal constructor(context: Context) : Preferences(context, "Default")

    class UI internal constructor(context: Context) : Preferences(context, "UI") {

        var isBlurHeader: Boolean by BooleanDelegate(true)

    }

    private class BooleanDelegate(private val def: Boolean) {
        operator fun getValue(preferences: Preferences, property: KProperty<*>): Boolean {
            return preferences.getBoolean(property.name, def)
        }

        operator fun setValue(preferences: Preferences, property: KProperty<*>, b: Boolean) {
            preferences.sharedPreferences.edit().putBoolean(property.name, b).apply()
        }
    }

    private class StringDelegate(private val def: String) {
        operator fun getValue(preferences: Preferences, property: KProperty<*>): String {
            return preferences.getString(property.name, def) ?: def
        }

        operator fun setValue(preferences: Preferences, property: KProperty<*>, b: String) {
            preferences.sharedPreferences.edit().putString(property.name, b).apply()
        }
    }

}