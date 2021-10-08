package com.lollipop.techo.data

import android.graphics.Color
import com.lollipop.gallery.Photo
import com.lollipop.gallery.PhotoGridLayout

/**
 * @author lollipop
 * @date 4/29/21 22:04
 */
data class TechoInfo(
    var title: String = "",
    var flag: TechoFlag = TechoFlag(),
    val items: MutableList<BaseTechoItem> = mutableListOf()
)

data class TechoFlag(
    var name: String = "",
    var color: Int = Color.RED,
    var id: Int = 0
)

enum class TechoItemType {
    Empty,
    Text,
    Number,
    CheckBox,
    Photo,
    Split;

    companion object {
        fun pauseFromOrdinal(ordinal: Int): TechoItemType {
            val valueArray = values()
            if (ordinal in valueArray.indices) {
                return valueArray[ordinal]
            }
            return valueArray[0]
        }
    }
}

object FontStyle {

    /**
     * 什么都没有
     */
    const val NORMAL = 0

    /**
     * 加粗
     */
    const val BOLD = 1

    /**
     * 斜体
     */
    const val ITALIC = 1 shl 1

    /**
     * 删除线
     */
    const val STRIKETHROUGH = 1 shl 2

    fun has(value: Int, flag: Int): Boolean {
        return value and flag != 0
    }

}

open class BaseTechoItem {

}

open class EmptyItem : BaseTechoItem()

open class TextItem(
    val values: MutableList<TextSpan> = mutableListOf()
) : BaseTechoItem()

open class TextSpan(
    var text: String = "",
    var color: Int = Color.BLACK,
    var style: Int
)

open class NumberItem(
    var number: Int = 0
) : TextItem()

open class CheckBoxItem(
    var isChecked: Boolean = false
) : TextItem()

class SplitItem : EmptyItem()

class PhotoItem(
    val values: MutableList<Photo> = mutableListOf(),
    val style: PhotoGridLayout.Style = PhotoGridLayout.Style.Playbill
): BaseTechoItem()
