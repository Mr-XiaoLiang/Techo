package com.lollipop.base.util.insets

import android.view.View
import androidx.core.view.WindowInsetsCompat

sealed class WindowInsetsApplyType {

    abstract fun onWindowInsetsChanged(
        view: View,
        edge: WindowInsetsEdge,
        baseMargin: BoundsSnapshot,
        basePadding: BoundsSnapshot,
        insets: WindowInsetsCompat
    ): WindowInsetsCompat

    object Padding : WindowInsetsApplyType() {
        override fun onWindowInsetsChanged(
            view: View,
            edge: WindowInsetsEdge,
            baseMargin: BoundsSnapshot,
            basePadding: BoundsSnapshot,
            insets: WindowInsetsCompat
        ): WindowInsetsCompat {
            val insetsValue = WindowInsetsHelper.getInsetsValue(insets)
            insetsValue.basePlus(basePadding, edge)
            WindowInsetsHelper.setPadding(
                view,
                insetsValue.left,
                insetsValue.top,
                insetsValue.right,
                insetsValue.bottom
            )
            return insets
        }

    }

    object Margin : WindowInsetsApplyType() {
        override fun onWindowInsetsChanged(
            view: View,
            edge: WindowInsetsEdge,
            baseMargin: BoundsSnapshot,
            basePadding: BoundsSnapshot,
            insets: WindowInsetsCompat
        ): WindowInsetsCompat {
            val insetsValue = WindowInsetsHelper.getInsetsValue(insets)
            insetsValue.basePlus(baseMargin, edge)
            WindowInsetsHelper.setMargin(
                view,
                insetsValue.left,
                insetsValue.top,
                insetsValue.right,
                insetsValue.bottom
            )
            return insets
        }

    }

    class Custom(
        private val listener: WindowInsetsHelper.OnWindowInsetsChangedListener
    ) : WindowInsetsApplyType() {
        override fun onWindowInsetsChanged(
            view: View,
            edge: WindowInsetsEdge,
            baseMargin: BoundsSnapshot,
            basePadding: BoundsSnapshot,
            insets: WindowInsetsCompat
        ): WindowInsetsCompat {
            return listener.onWindowInsetsChanged(
                view,
                WindowInsetsOption(edge, baseMargin, basePadding),
                insets
            )
        }
    }
}