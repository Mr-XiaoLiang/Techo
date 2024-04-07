package com.lollipop.punch2.base

import androidx.activity.ComponentActivity
import androidx.annotation.CallSuper
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.MutableLiveData
import com.lollipop.punch2.utils.ThemeHelper

open class LComponentActivity : ComponentActivity() {

    protected val themeHelper = ThemeHelper.createWith(this) { theme, dark ->
        onThemeChanged(theme, dark)
    }

    private val themeLive = MutableLiveData<ColorScheme>()

    @CallSuper
    protected open fun onThemeChanged(theme: ColorScheme, isDark: Boolean) {
        this.themeLive.postValue(theme)
    }

    @Composable
    protected fun liveTheme() = themeLive.observeAsState()

}