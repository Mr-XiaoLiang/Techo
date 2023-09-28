package com.lollipop.lbus.core

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import com.lollipop.lbus.LBus
import kotlin.reflect.KProperty

open class LBusEvent(initIntent: Intent = Intent()) {

    var intent: Intent = initIntent
        private set

    var action: String
        get() {
            return intent.action ?: ""
        }
        set(value) {
            intent.action = value
        }

    init {
        if (action.isEmpty()) {
            action = getDefaultAction()
        }
    }

    fun parse(intent: Intent) {
        this.intent = intent
    }

    private fun getDefaultAction(): String {
        return this::class.java.name
    }

    fun send() {
        LBus.send(this)
    }

    protected fun self(def: String, key: String = "") = StringDelegate(key, def)
    protected fun self(def: Int, key: String = "") = IntDelegate(key, def)
    protected fun self(def: Long, key: String = "") = LongDelegate(key, def)
    protected fun self(def: Double, key: String = "") = DoubleDelegate(key, def)
    protected fun self(def: Float, key: String = "") = FloatDelegate(key, def)
    protected fun self(def: Boolean, key: String = "") = BooleanDelegate(key, def)
    protected fun <T : Parcelable> self(
        clazz: Class<T>, key: String = "", def: () -> T
    ) = ParcelableDelegate(key, clazz, def)


    protected class StringDelegate(private val key: String = "", private val def: String = "") {
        operator fun getValue(event: LBusEvent, property: KProperty<*>): String {
            return event.intent.getStringExtra(key.ifEmpty { property.name }) ?: def
        }

        operator fun setValue(event: LBusEvent, property: KProperty<*>, s: String) {
            event.intent.putExtra(key.ifEmpty { property.name }, s)
        }

    }

    protected class IntDelegate(private val key: String = "", private val def: Int = 0) {
        operator fun getValue(event: LBusEvent, property: KProperty<*>): Int {
            return event.intent.getIntExtra(key.ifEmpty { property.name }, def)
        }

        operator fun setValue(event: LBusEvent, property: KProperty<*>, i: Int) {
            event.intent.putExtra(key.ifEmpty { property.name }, i)
        }

    }

    protected class LongDelegate(private val key: String = "", private val def: Long = 0L) {
        operator fun getValue(event: LBusEvent, property: KProperty<*>): Long {
            return event.intent.getLongExtra(key.ifEmpty { property.name }, def)
        }

        operator fun setValue(event: LBusEvent, property: KProperty<*>, s: Long) {
            event.intent.putExtra(key.ifEmpty { property.name }, s)
        }

    }

    protected class BooleanDelegate(
        private val key: String = "",
        private val def: Boolean = false
    ) {
        operator fun getValue(event: LBusEvent, property: KProperty<*>): Boolean {
            return event.intent.getBooleanExtra(key.ifEmpty { property.name }, def)
        }

        operator fun setValue(event: LBusEvent, property: KProperty<*>, s: Boolean) {
            event.intent.putExtra(key.ifEmpty { property.name }, s)
        }

    }

    protected class FloatDelegate(private val key: String = "", private val def: Float = 0F) {
        operator fun getValue(event: LBusEvent, property: KProperty<*>): Float {
            return event.intent.getFloatExtra(key.ifEmpty { property.name }, def)
        }

        operator fun setValue(event: LBusEvent, property: KProperty<*>, s: Float) {
            event.intent.putExtra(key.ifEmpty { property.name }, s)
        }

    }

    protected class DoubleDelegate(private val key: String = "", private val def: Double = 0.0) {
        operator fun getValue(event: LBusEvent, property: KProperty<*>): Double {
            return event.intent.getDoubleExtra(key.ifEmpty { property.name }, def)
        }

        operator fun setValue(event: LBusEvent, property: KProperty<*>, s: Double) {
            event.intent.putExtra(key.ifEmpty { property.name }, s)
        }

    }

    protected class ParcelableDelegate<T : Parcelable>(
        private val key: String = "",
        private val clazz: Class<T>,
        private val def: () -> T
    ) {
        operator fun getValue(event: LBusEvent, property: KProperty<*>): T {
            val name = key.ifEmpty { property.name }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                event.intent.getParcelableExtra(name, clazz) ?: def()
            } else {
                event.intent.getParcelableExtra(name) ?: def()
            }
        }

        operator fun setValue(event: LBusEvent, property: KProperty<*>, s: T) {
            event.intent.putExtra(key.ifEmpty { property.name }, s)
        }

    }

}