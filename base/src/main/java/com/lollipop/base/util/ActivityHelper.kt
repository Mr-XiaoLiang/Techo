package com.lollipop.base.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent

object ActivityHelper {

    fun isActivity(context: Context): Boolean {
        var c: Context = context
        do {
            if (c is Activity) {
                return true
            }
            if (c is ContextWrapper) {
                c = c.baseContext
            } else {
                return false
            }
        } while (true)
    }

    inline fun <reified T : Activity> start(
        context: Context,
        intentCallback: ((Intent) -> Unit) = {},
    ) {
        val intent = Intent(context, T::class.java)
        intentCallback(intent)
        start(context, intent)
    }

    fun start(context: Context, intent: Intent) {
        if (!isActivity(context)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

}
