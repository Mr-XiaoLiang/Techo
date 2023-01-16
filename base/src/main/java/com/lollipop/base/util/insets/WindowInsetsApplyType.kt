package com.lollipop.base.util.insets

import android.view.View
import androidx.core.view.WindowInsetsCompat

sealed class WindowInsetsApplyType {

    abstract fun onWindowInsetsChanged(
        view: View,
        operator: WindowInsetsOperator,
        insets: WindowInsetsCompat
    ): WindowInsetsCompat

    object Padding : WindowInsetsApplyType() {
        override fun onWindowInsetsChanged(
            view: View,
            operator: WindowInsetsOperator,
            insets: WindowInsetsCompat
        ): WindowInsetsCompat {
            val insetsValue = operator.computeInsetsValueByPadding(insets)
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
            operator: WindowInsetsOperator,
            insets: WindowInsetsCompat
        ): WindowInsetsCompat {
            val insetsValue = operator.computeInsetsValueByMargin(insets)
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
            operator: WindowInsetsOperator,
            insets: WindowInsetsCompat
        ): WindowInsetsCompat {
            return listener.onWindowInsetsChanged(
                view,
                operator,
                insets
            )
        }
    }
}