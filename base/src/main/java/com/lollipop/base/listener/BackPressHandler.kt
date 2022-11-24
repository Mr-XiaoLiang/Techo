package com.lollipop.base.listener

import android.app.Activity
import android.app.Dialog
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

class BackPressHandler(
    enable: Boolean = false,
    private val listener: BackPressListener
) : OnBackPressedCallback(enable) {

    companion object {
        fun findDispatcher(view: View): Dispatcher {
            if (view is OnBackPressedDispatcherOwner) {
                return Dispatcher(view, null, null)
            }
            view.context.let {
                if (it is OnBackPressedDispatcherOwner) {
                    return Dispatcher(it, null, null)
                }
            }
            return Dispatcher(null, null, null)
        }

        fun findDispatcher(activity: Activity): Dispatcher {
            if (activity is OnBackPressedDispatcherOwner) {
                return Dispatcher(activity, null, activity)
            }
            return Dispatcher(null, null, null)
        }

        fun findDispatcher(fragment: Fragment): Dispatcher {
            if (fragment is OnBackPressedDispatcherOwner) {
                return Dispatcher(fragment, null, fragment)
            }
            fragment.parentFragment?.let {
                if (it is OnBackPressedDispatcherOwner) {
                    return Dispatcher(it, null, fragment)
                }
            }
            fragment.activity?.let {
                return Dispatcher(it, null, fragment)
            }
            return Dispatcher(null, null, null)
        }

        fun findDispatcher(dialog: Dialog): Dispatcher {
            if (dialog is OnBackPressedDispatcherOwner) {
                return Dispatcher(dialog, null, null)
            }
            dialog.context.let {
                if (it is OnBackPressedDispatcherOwner) {
                    return Dispatcher(it, null, null)
                }
            }
            return Dispatcher(null, null, null)
        }
    }

    override fun handleOnBackPressed() {
        listener.onBackPressed()
    }

    inline fun <reified T : Any> tryBindTo(any: T, lifecycleOwner: LifecycleOwner? = null) {
        if (any is OnBackPressedDispatcher) {
            bindTo(any, lifecycleOwner)
        } else if (any is OnBackPressedDispatcherOwner) {
            bindTo(any, lifecycleOwner)
        }
    }

    fun bindTo(
        dispatcherOwner: OnBackPressedDispatcherOwner,
        lifecycleOwner: LifecycleOwner? = null
    ) {
        bindTo(dispatcherOwner.onBackPressedDispatcher, lifecycleOwner ?: dispatcherOwner)
    }

    fun bindTo(
        dispatcher: OnBackPressedDispatcher,
        lifecycleOwner: LifecycleOwner?
    ) {
        if (lifecycleOwner != null) {
            dispatcher.addCallback(lifecycleOwner, this)
        } else {
            dispatcher.addCallback(this)
        }
    }

    class Dispatcher(
        private val owner: OnBackPressedDispatcherOwner?,
        private val dispatcher: OnBackPressedDispatcher?,
        private val lifecycleOwner: LifecycleOwner?
    ) {

        fun bind(handler: BackPressHandler) {
            if (dispatcher != null) {
                handler.bindTo(dispatcher, lifecycleOwner)
            } else if (owner != null) {
                handler.bindTo(owner, lifecycleOwner)
            }
        }

    }

}