package com.lollipop.techo.data

import android.graphics.Color
import com.lollipop.techo.data.json.JsonObjectInfo

/**
 * @author lollipop
 * @date 4/29/21 22:04
 */
class TechoInfo: JsonObjectInfo() {

    var title by withThis("")

    var color by withThis(Color.TRANSPARENT)

}