package com.lollipop.techo.edit.base

import android.view.View
import com.lollipop.techo.data.TechoItem

abstract class BottomEditDelegate<T : TechoItem> : EditDelegate<T>() {

    abstract val contentGroup: View?

    abstract val backgroundView: View?

    override val animationEnable: Boolean
        get() = true

    override fun onAnimationUpdate(progress: Float) {
        super.onAnimationUpdate(progress)
        backgroundView?.let {
            animationAlpha(progress, it)
        }
        contentGroup?.let {
            animationUp(progress, it)
        }
    }

}