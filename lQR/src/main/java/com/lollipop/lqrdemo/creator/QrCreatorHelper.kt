package com.lollipop.lqrdemo.creator

class QrCreatorHelper {

    var contentValue: String = ""
        set(value) {
            field = value
            onContentChanged()
        }

    private fun onContentChanged() {
        // TODO
    }

}