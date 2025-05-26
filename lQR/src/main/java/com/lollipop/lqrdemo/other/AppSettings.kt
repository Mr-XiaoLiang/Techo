package com.lollipop.lqrdemo.other

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.lollipop.lqrdemo.QrApplication
import kotlin.reflect.KProperty

sealed class AppSettings private constructor(
    private val sharedPreferences: SharedPreferences,
) : SharedPreferences by sharedPreferences {

    companion object {
        val ui by lazy {
            UI(QrApplication.APP)
        }

        val default by lazy {
            Default(QrApplication.APP)
        }

        const val FLOATING_SCAN_BUTTON_SIZE_MIN = 32
        const val FLOATING_SCAN_BUTTON_SIZE_DEFAULT = 48
        const val FLOATING_SCAN_BUTTON_SIZE_MAX = 64

    }

    constructor(context: Context, name: String) : this(
        context.getSharedPreferences(
            name,
            Context.MODE_PRIVATE
        )
    )

    class Default internal constructor(context: Context) : AppSettings(context, "Default") {
        var isAgreePrivacyAgreement: Boolean by BooleanDelegate(false)
    }

    class UI internal constructor(context: Context) : AppSettings(context, "UI") {

        var floatingScanButtonSize: Int by IntDelegate(FLOATING_SCAN_BUTTON_SIZE_DEFAULT)

    }

    private class BooleanDelegate(private val def: Boolean) {
        operator fun getValue(preferences: AppSettings, property: KProperty<*>): Boolean {
            return preferences.getBoolean(property.name, def)
        }

        operator fun setValue(preferences: AppSettings, property: KProperty<*>, b: Boolean) {
            preferences.sharedPreferences.edit { putBoolean(property.name, b) }
        }
    }

    private class IntDelegate(private val def: Int) {
        operator fun getValue(preferences: AppSettings, property: KProperty<*>): Int {
            return preferences.getInt(property.name, def)
        }

        operator fun setValue(preferences: AppSettings, property: KProperty<*>, b: Int) {
            preferences.sharedPreferences.edit { putInt(property.name, b) }
        }
    }

    private class StringDelegate(private val def: String) {
        operator fun getValue(preferences: AppSettings, property: KProperty<*>): String {
            return preferences.getString(property.name, def) ?: def
        }

        operator fun setValue(preferences: AppSettings, property: KProperty<*>, b: String) {
            preferences.sharedPreferences.edit { putString(property.name, b) }
        }
    }

}