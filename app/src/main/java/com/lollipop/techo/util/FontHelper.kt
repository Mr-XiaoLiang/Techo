package com.lollipop.techo.util

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.widget.TextView
import androidx.annotation.Keep
import org.json.JSONObject

object FontHelper {

    private const val PREFERENCES = "FONT_PREFERENCES"
    private const val KEY_FONT_TYPE = "FONT_TYPE"

    private val fontType = Array(Type.values().size) { FontInfo(Font.None) }

    fun setFont(view: TextView, font: Font) {
        if (font == Font.None) {
            view.typeface = null
            return
        }
        view.typeface = Typeface.createFromAsset(view.context.assets, "font/${font.ttf}")
    }

    fun setFont(view: TextView, type: Type) {
        setFont(view, getFontType(type))
    }

    private fun setFontType(type: Type, font: Font) {
        fontType[type.ordinal] = FontInfo(font)
    }

    private fun getFontType(type: Type): Font {
        return fontType[type.ordinal].font
    }

    fun init(context: Context) {
        val preferences = getPreferences(context)
        val value = preferences.getString(KEY_FONT_TYPE, "") ?: ""
        if (value.isEmpty()) {
            resetFontType()
            return
        }
        try {
            val json = JSONObject(value)
            val typeArray = Type.values()
            for (i in typeArray.indices) {
                val type = typeArray[i]
                setFontType(
                    type,
                    findFontByTtf(json.optString(type.name))
                )
            }
        } catch (e: Throwable) {
            resetFontType()
        }
    }

    private fun resetFontType() {
        for (index in fontType.indices) {
            fontType[index] = FontInfo(Font.None)
        }
    }

    private fun findFontByTtf(name: String): Font {
        return Font.values().find { it.ttf == name } ?: Font.None
    }

    fun changeFontType(type: Type, font: Font) {
        fontType[type.ordinal].pending = font
    }

    fun applyChanged(context: Context) {
        val preferences = getPreferences(context)
        val json = JSONObject()
        val typeArray = Type.values()
        for (index in fontType.indices) {
            val type = typeArray[index]
            val pending = fontType[index].pending
            json.put(type.name, pending.ttf)
        }
        val edit = preferences.edit()
        edit.putString(KEY_FONT_TYPE, json.toString())
        edit.apply()
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    }

    @Keep
    enum class Type {
        HomeListDay,
        HomeListMonth,
    }

    private class FontInfo(
        val font: Font,
        var pending: Font = font
    )

}

@Keep
enum class Font(val ttf: String) {
    None(""),
    CabinSketch("CabinSketch-Regular.ttf"),
    Catamaran("Catamaran-Regular.ttf"),
    Dynalight("Dynalight-Regular.ttf"),
    FrederickaTheGreat("FrederickaTheGreat-Regular.ttf"),
    Limelight("Limelight-Regular.ttf"),
    MissFajarDose("MissFajarDose-Regular.ttf"),
    Monoton("Monoton-Regular.ttf"),
    Oregano("Oregano-Regular.ttf"),
    PlayBall("Playball-Regular.ttf"),
    PoiretOne("PoiretOne-Regular.ttf"),
    RougeScript("RougeScript-Regular.ttf"),
    Ruthie("Ruthie-Regular.ttf"),
    Tangerine("Tangerine-Regular.ttf")
}

fun TextView.setTypeface(font: Font) {
    FontHelper.setFont(this, font)
}

fun TextView.setTypeface(type: FontHelper.Type) {
    FontHelper.setFont(this, type)
}
