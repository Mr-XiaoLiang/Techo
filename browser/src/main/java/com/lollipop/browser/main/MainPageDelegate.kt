package com.lollipop.browser.main

import android.view.ViewGroup
import com.lollipop.base.util.bind
import com.lollipop.browser.databinding.PageMainBinding

class MainPageDelegate(private val binding: PageMainBinding) {

    companion object {
        fun inflate(container: ViewGroup): MainPageDelegate {
            return MainPageDelegate(container.bind(true))
        }
    }

    fun resume() {
        // TODO
    }

    fun destroy() {
        // TODO
    }

}