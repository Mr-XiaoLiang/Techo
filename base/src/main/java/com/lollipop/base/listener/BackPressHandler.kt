package com.lollipop.base.listener

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner

class BackPressHandler(
    enable: Boolean = false,
    private val listener: BackPressListener
) : OnBackPressedCallback(enable) {

    override fun handleOnBackPressed() {
        listener.onBackPressed()
    }

    fun bindTo(
        dispatcherOwner: OnBackPressedDispatcherOwner,
        lifecycleOwner: LifecycleOwner? = null
    ) {
        dispatcherOwner.onBackPressedDispatcher.addCallback(
            lifecycleOwner ?: dispatcherOwner,
            this
        )
    }

}

fun AppCompatActivity.backHandler(
    enable: Boolean = true,
    listener: BackPressListener
): BackPressHandler {
    val backPressHandler = BackPressHandler(enable, listener)
    backPressHandler.bindTo(this, null)
    return backPressHandler
}