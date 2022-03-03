package com.lollipop.techo.util

import android.graphics.Typeface
import android.widget.TextView

object FontHelper {

    fun setFont(view: TextView, font: Font) {
        view.typeface = Typeface.createFromAsset(view.context.assets, "font/${font.ttf}")
    }

}

enum class Font(val ttf: String) {
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
