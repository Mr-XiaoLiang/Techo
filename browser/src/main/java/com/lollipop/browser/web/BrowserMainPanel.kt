package com.lollipop.browser.web

import android.view.ViewGroup
import android.view.ViewManager
import com.lollipop.base.util.bind
import com.lollipop.browser.databinding.PanelBrowserMainBinding

class BrowserMainPanel(
    private val binding: PanelBrowserMainBinding
) {

    companion object {
        fun bind(container: ViewGroup): BrowserMainPanel {
            return BrowserMainPanel(container.bind(true))
        }
    }

    fun remove() {
        val root = binding.root
        val parent = root.parent ?: return
        if (parent is ViewManager) {
            parent.removeView(root)
        }
    }

    interface Callback {

    }

}