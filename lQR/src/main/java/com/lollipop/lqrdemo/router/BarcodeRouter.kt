package com.lollipop.lqrdemo.router

import android.content.Context
import android.content.Intent
import com.lollipop.qr.comm.BarcodeInfo

abstract class BarcodeRouter<T : BarcodeInfo> {

    open fun open(context: Context, barcodeInfo: T): Boolean {
        try {
            val intent = getIntent(context, barcodeInfo) ?: return false
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }

    protected open fun getIntent(context: Context, barcodeInfo: T): Intent? {
        return null
    }

    protected inline fun <reified T : Any> List<T>.findFirst(): T? {
        if (isEmpty()) {
            return null
        }
        return this[0]
    }

    protected inline fun <reified T : Any> List<T>.findSecondary(): T? {
        if (size < 2) {
            return null
        }
        return this[1]
    }

    protected inline fun <reified T : Any> List<T>.findTertiary(): T? {
        if (size < 3) {
            return null
        }
        return this[2]
    }

}