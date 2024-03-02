package com.lollipop.techo.data

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle

sealed class TechoModeBuilder(private val context: Context) {

    protected var lifecycle: Lifecycle? = null

    protected fun findLifecycle(): Lifecycle? {
        val l = lifecycle
        if (l != null) {
            return l
        }
        return findActivity()?.lifecycle
    }

    private fun findActivity(): AppCompatActivity? {
        var c: Context? = context
        do {
            if (c is AppCompatActivity) {
                return c
            }
            if (c is ContextWrapper) {
                c = c.baseContext
            } else {
                return null
            }
        } while (true)
    }

    class EditBuilder(private val context: Context) : TechoModeBuilder(context) {
        private var listener: TechoMode.EditStateListener? = null

        fun attach(listener: TechoMode.EditStateListener): EditBuilder {
            this.listener = listener
            return this
        }

        fun bind(lifecycle: Lifecycle): EditBuilder {
            this.lifecycle = lifecycle
            return this
        }

        fun build(): TechoMode.EditMode {
            val lis = listener ?: EmptyListener()
            return TechoMode.EditMode(lis, findLifecycle(), context)
        }

    }

    class DetailBuilder(private val context: Context) : TechoModeBuilder(context) {
        private var listener: TechoMode.DetailStateListener? = null

        fun attach(listener: TechoMode.DetailStateListener): DetailBuilder {
            this.listener = listener
            return this
        }

        fun bind(lifecycle: Lifecycle): DetailBuilder {
            this.lifecycle = lifecycle
            return this
        }

        fun build(): TechoMode.DetailMode {
            val lis = listener ?: EmptyListener()
            return TechoMode.DetailMode(lis, findLifecycle(), context)
        }

    }

    class ListBuilder(private val context: Context) : TechoModeBuilder(context) {
        private var listener: TechoMode.ListStateListener? = null

        fun attach(listener: TechoMode.ListStateListener): ListBuilder {
            this.listener = listener
            return this
        }

        fun bind(lifecycle: Lifecycle): ListBuilder {
            this.lifecycle = lifecycle
            return this
        }

        fun build(): TechoMode.ListMode {
            val lis = listener ?: EmptyListener()
            return TechoMode.ListMode(lis, findLifecycle(), context)
        }

    }

    private class EmptyListener : TechoMode.EditStateListener, TechoMode.DetailStateListener,
        TechoMode.ListStateListener {
        override fun onInfoChanged(info: TechoInfo) {

        }

        override fun onLoadStart() {
        }

        override fun onLoadEnd() {
        }

        override fun onInfoChanged(first: Int, second: Int, type: TechoMode.ChangedType) {
        }
    }

}