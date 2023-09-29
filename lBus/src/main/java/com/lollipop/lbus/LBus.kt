package com.lollipop.lbus

import android.content.Context
import android.content.IntentFilter
import com.lollipop.lbus.core.LBusEvent
import com.lollipop.lbus.core.LBusManager
import com.lollipop.lbus.core.LBusReceiver

object LBus {

    fun init(context: Context) {
        LBusManager.init(context)
    }

    fun register(receiver: LBusReceiver<*>, intentFilter: IntentFilter) {
        LBusManager.register(receiver, intentFilter)
    }

    fun register(receiver: LBusReceiver<*>, actionArray: Array<String>) {
        if (actionArray.isEmpty()) {
            throw IllegalArgumentException("action is empty")
        }
        val intentFilter = IntentFilter()
        actionArray.forEach { action ->
            intentFilter.addAction(action)
        }
        register(receiver, intentFilter)
    }

    fun send(lBusEvent: LBusEvent) {
        LBusManager.send(lBusEvent)
    }

}

