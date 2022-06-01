package com.lollipop.techo.data

import android.graphics.Color
import androidx.annotation.ColorInt
import com.lollipop.techo.data.json.JsonInfo
import org.json.JSONObject

class TechoFlag : JsonInfo {

    companion object {
        private const val KEY_FLAG_ID = "flagId"
        private const val KEY_FLAG_COLOR = "flagColor"
        private const val KEY_FLAG_NAME = "flagName"

        fun create(
            id: Int,
            name: String,
            color: Int
        ): TechoFlag {
            val flag = TechoFlag()
            flag.id = id
            flag.name = name
            flag.color = color
            return flag
        }

    }

    var id: Int = 0
    var name: String = ""

    @ColorInt
    var color: Int = Color.RED

    fun copyTo(newFlag: TechoFlag = TechoFlag()): TechoFlag {
        newFlag.id = id
        newFlag.color = color
        newFlag.name = name
        return newFlag
    }

    fun baseTo(
        id: Int = this.id,
        name: String = this.name,
        color: Int = this.color,
    ): TechoFlag {
        val newFlag = TechoFlag()
        newFlag.id = id
        newFlag.name = name
        newFlag.color = color
        return newFlag
    }

    override fun toJson(): JSONObject {
        return JSONObject().apply {
            put(KEY_FLAG_ID, id)
            put(KEY_FLAG_NAME, name)
            put(KEY_FLAG_COLOR, color)
        }
    }

    override fun parse(json: JSONObject) {
        id = json.optInt(KEY_FLAG_ID)
        name = json.optString(KEY_FLAG_NAME)
        color = json.optInt(KEY_FLAG_COLOR)
    }
}