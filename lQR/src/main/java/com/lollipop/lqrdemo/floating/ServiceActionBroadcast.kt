package com.lollipop.lqrdemo.floating

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

class ServiceActionBroadcast private constructor(
    private val callbackMap: HashMap<String, OnReceiveCallback>
) : BroadcastReceiver() {

    companion object {
        const val ACTION_SCREENSHOT = "lollipop.lqr.Action.Screenshot"
        const val ACTION_STOP = "lollipop.lqr.Action.Stop"
        const val ACTION_HIDE_ACTION_BUTTON = "lollipop.lqr.Action.hide_action_button"
        const val ACTION_SHOW_ACTION_BUTTON = "lollipop.lqr.Action.show_action_button"

        fun sendBroadcast(context: Context, action: String) {
            context.sendBroadcast(getIntent(context, action))
        }

        fun getIntent(context: Context, action: String): Intent {
            return Intent(action).setPackage(context.packageName)
        }

        fun sendHideActionButton(context: Context) {
            sendBroadcast(context, ACTION_HIDE_ACTION_BUTTON)
        }

        fun sendShowActionButton(context: Context) {
            sendBroadcast(context, ACTION_SHOW_ACTION_BUTTON)
        }

        fun sendStop(context: Context) {
            sendBroadcast(context, ACTION_STOP)
        }

        fun sendScreenshot(context: Context) {
            sendBroadcast(context, ACTION_SCREENSHOT)
        }

        fun receiver(): Builder {
            return Builder()
        }

    }

    private val intentFilter by lazy {
        IntentFilter().apply {
            callbackMap.keys.forEach {
                addAction(it)
            }
        }
    }

    fun attach(context: Context) {
        ContextCompat.registerReceiver(
            context,
            this,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun detach(context: Context) {
        context.unregisterReceiver(this)
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        val action = intent?.action ?: return
        callbackMap[action]?.onReceive(context)
    }

    fun interface OnReceiveCallback {
        fun onReceive(context: Context)
    }

    class Builder {
        private val callbackMap = HashMap<String, OnReceiveCallback>()
        fun register(action: String, callback: OnReceiveCallback): Builder {
            callbackMap[action] = callback
            return this
        }

        fun screenshot(
            callback: OnReceiveCallback
        ): Builder {
            return register(ACTION_SCREENSHOT, callback)
        }

        fun stop(callback: OnReceiveCallback): Builder {
            return register(ACTION_STOP, callback)
        }

        fun hideActionButton(callback: OnReceiveCallback): Builder {
            return register(ACTION_HIDE_ACTION_BUTTON, callback)
        }

        fun showActionButton(callback: OnReceiveCallback): Builder {
            return register(ACTION_SHOW_ACTION_BUTTON, callback)
        }

        fun build(): ServiceActionBroadcast {
            return ServiceActionBroadcast(callbackMap)
        }

    }

}
