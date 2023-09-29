package com.lollipop.lbus.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lollipop.lbus.LBus
import java.util.LinkedList

abstract class LBusReceiver<T : LBusEvent>(
    private val minLifecycle: Lifecycle.State
) : BroadcastReceiver(),
    LifecycleEventObserver {

    companion object {

        fun <E : LBusEvent> createEvent(intent: Intent, clazz: Class<E>): E {
            try {
                val constructor = clazz.getConstructor(Intent::class.java)
                return constructor.newInstance(intent)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            val constructor = clazz.getConstructor()
            val newInstance = constructor.newInstance()
            newInstance.parse(intent)
            return newInstance
        }

    }

    private val pendingIntentList = LinkedList<Intent>()

    private var currentState: Lifecycle.State = Lifecycle.State.INITIALIZED

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        if (currentState.isAtLeast(minLifecycle)) {
            releasePending()
            onReceive(createEvent(intent))
        } else {
            pushPending(intent)
        }
    }

    private fun releasePending() {
        while (pendingIntentList.isNotEmpty()) {
            val first = pendingIntentList.removeFirst()
            onReceive(createEvent(first))
        }
    }

    private fun pushPending(intent: Intent) {
        pendingIntentList.addLast(intent)
    }

    abstract fun createEvent(intent: Intent): T

    abstract fun onReceive(intent: T)

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        currentState = event.targetState
        if (source.lifecycle.currentState == Lifecycle.State.DESTROYED || event == Lifecycle.Event.ON_DESTROY) {
            source.lifecycle.removeObserver(this)
            destroy()
            return
        }
        if (currentState.isAtLeast(minLifecycle)) {
            releasePending()
        }
    }

    fun destroy() {
        pendingIntentList.clear()
        LBusManager.unregister(this)
    }

    class Simple<T : LBusEvent>(
        minLifecycle: Lifecycle.State = Lifecycle.State.RESUMED,
        private val decoder: (Intent) -> T,
        private val callback: (T) -> Unit
    ) : LBusReceiver<T>(minLifecycle) {

        override fun onReceive(intent: T) {
            callback(intent)
        }

        override fun createEvent(intent: Intent): T {
            return decoder(intent)
        }

    }

}

inline fun <reified T : LBusEvent> LifecycleOwner.bindLBus(
    minLifecycle: Lifecycle.State = Lifecycle.State.RESUMED,
    actionArray: Array<String> = emptyArray(),
    noinline callback: (T) -> Unit
): LBusReceiver.Simple<T> {
    val creator: (Intent) -> T = { intent: Intent ->
        LBusReceiver.createEvent(intent, T::class.java)
    }
    val simple = LBusReceiver.Simple(minLifecycle, creator, callback)
    lifecycle.addObserver(simple)
    val actions = if (actionArray.isEmpty()) {
        arrayOf(T::class.java.name)
    } else {
        actionArray
    }
    LBus.register(simple, actions)
    return simple
}
