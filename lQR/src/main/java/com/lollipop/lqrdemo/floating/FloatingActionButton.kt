package com.lollipop.lqrdemo.floating

import android.content.Context
import android.view.View
import com.lollipop.lqrdemo.floating.view.FloatingView
import com.lollipop.lqrdemo.floating.view.FloatingViewBerth
import com.lollipop.lqrdemo.floating.view.FloatingViewConfig
import com.lollipop.lqrdemo.floating.view.FloatingViewFactory
import com.lollipop.lqrdemo.floating.view.FloatingViewState

class FloatingActionButton : FloatingView {

    companion object {
        val CONFIG = FloatingViewConfig()
    }

    override fun createView(
        context: Context,
        widthDp: Int,
        heightDp: Int,
        berthWeight: Float
    ): View {
        TODO("Not yet implemented")
    }

    override fun onBerthChanged(berth: FloatingViewBerth) {
        TODO("Not yet implemented")
    }

    override fun onStateChanged(state: FloatingViewState) {
        TODO("Not yet implemented")
    }

    object Factory : FloatingViewFactory {
        override fun create(): FloatingView {
            return FloatingActionButton()
        }
    }

}

