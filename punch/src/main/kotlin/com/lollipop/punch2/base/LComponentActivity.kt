package com.lollipop.punch2.base

import androidx.activity.ComponentActivity
import androidx.compose.material3.ColorScheme
import androidx.lifecycle.MutableLiveData
import com.lollipop.punch2.utils.ThemeHelper

open class LComponentActivity : ComponentActivity() {

    protected val themeHelper = ThemeHelper.createWith(this) { theme, dark ->
        onThemeChanged(theme, dark)
    }

    protected val themeLive = MutableLiveData<ColorScheme>()

    protected open fun onThemeChanged(theme: ColorScheme, isDark: Boolean) {
        this.themeLive.value = theme
    }

}