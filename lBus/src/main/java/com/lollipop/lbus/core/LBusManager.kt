package com.lollipop.lbus.core

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager


object LBusManager {

    private var application: Context? = null

    /**
     * LocalBroadcastManager中也是一个单例，所以我们可以也使用一个单例保存起来，方便使用的过程中调用
     * 就不需要每次都获取上下文了
     */
    private var localBroadcastManager: LocalBroadcastManager? = null

    fun init(context: Context) {
        val applicationContext = context.applicationContext
        application = applicationContext
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
    }

    fun register(receiver: LBusReceiver<*>, intentFilter: IntentFilter) {
        localBroadcastManager?.registerReceiver(receiver, intentFilter)
    }

    fun unregister(receiver: LBusReceiver<*>) {
        localBroadcastManager?.unregisterReceiver(receiver)
    }

    fun send(intent: Intent) {
        localBroadcastManager?.sendBroadcast(intent)
    }

}