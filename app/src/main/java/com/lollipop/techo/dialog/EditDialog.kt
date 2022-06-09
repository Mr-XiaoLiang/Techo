package com.lollipop.techo.dialog

import com.lollipop.techo.data.TechoItem

object EditDialog {

    fun <T : TechoItem> open(index: Int, item: T, onChanged: OnInfoChangedCallback) {
        when (item) {
            is TechoItem.Text,
            is TechoItem.Number,
            is TechoItem.CheckBox,
            is TechoItem.Title -> {
                // TODO
            }
            is TechoItem.Split -> {
                // TODO
            }
            is TechoItem.Photo -> {
                // TODO
            }
        }
    }

    fun interface OnInfoChangedCallback {
        fun onInfoChanged()
    }

}