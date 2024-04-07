package com.lollipop.punch2.utils

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.ComponentActivity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class ThemeHelper {

    companion object {

        private const val PROGRESS_MAX = 1F
        private const val PROGRESS_MIN = 0F
        private const val DURATION = 10 * 1000L

        var isDarkMode: Boolean = false
            private set

        val colorBlend: ColorBlend
            get() {
                return if (isDarkMode) {
                    ColorBlend.Dark
                } else {
                    ColorBlend.Light
                }
            }

        private fun defaultTheme(context: Context): ColorScheme {
            return if (isDark()) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        var currentTheme: ColorScheme? = null
            private set

        fun getTheme(context: Context): ColorScheme {
            val theme = currentTheme
            if (theme != null) {
                return theme
            }
            val defaultTheme = defaultTheme(context)
            currentTheme = defaultTheme
            return defaultTheme
        }

        fun createWith(activity: ComponentActivity, callback: OnThemeChangeCallback): ThemeHelper {
            val helper = ThemeHelper()
            helper.bindLifecycle(activity)
            helper.onThemeChanged(callback)
            return helper
        }

        private fun buildHeaderColor(isDark: Boolean, themeColor: Int): Int {
            val baseColor = if (isDark) {
                Color.BLACK
            } else {
                Color.WHITE
            }
            return ColorUtils.blendARGB(baseColor, themeColor, 0.2F)
        }

        private fun isDark(): Boolean {
            val resources = Resources.getSystem()
            val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val b = uiMode == Configuration.UI_MODE_NIGHT_YES
            isDarkMode = b
            return b
        }

    }

    val themeLive = MutableLiveData<ColorScheme>()

    val theme: ColorScheme?
        get() {
            return themeLive.value
        }

    private val drawable = BackgroundDrawable()

    private val animator = ValueAnimator()

    private var progress = PROGRESS_MAX

    private var onThemeChangeCallback: OnThemeChangeCallback? = null

    val isStarted: Boolean
        get() {
            return animator.isStarted
        }

    init {
        animator.setFloatValues(PROGRESS_MAX, PROGRESS_MIN)
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.duration = DURATION
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener {
            val value = it.animatedValue
            if (value is Float) {
                onProgressUpdate(value)
            }
        }
    }

    fun bindLifecycle(activity: ComponentActivity) {
        activity.lifecycle.addObserver(
            ActivityDelegate(
                activity = activity,
                backgroundHelper = this
            )
        )
    }

    fun bindView(view: View) {
        view.background = drawable
    }

    fun onThemeChanged(callback: OnThemeChangeCallback) {
        this.onThemeChangeCallback = callback
    }

    private fun changeColor(color: Int) {
        drawable.color = color
    }

    private fun onStart(context: Context) {
        val dark = isDark()
        val colorScheme = if (dark) {
            dynamicLightColorScheme(context)
        } else {
            dynamicDarkColorScheme(context)
        }
        currentTheme = colorScheme
        val themeColor = colorScheme.primary.toArgb()
        changeColor(buildHeaderColor(dark, themeColor))
        onThemeChangeCallback?.onThemeChanged(colorScheme, dark)
        themeLive.postValue(colorScheme)
        if (drawable.callback != null) {
            animator.start()
        }
    }

    private fun onStop() {
        animator.cancel()
    }

    private fun onProgressUpdate(p: Float) {
        progress = p
        if (progress < PROGRESS_MIN) {
            progress = PROGRESS_MIN
        }
        if (progress > PROGRESS_MAX) {
            progress = PROGRESS_MAX
        }
        updateByProgress()
    }

    private fun updateByProgress() {
        val minAlpha = 55
        val maxAlpha = 255
        val progressAlpha = (progress * (maxAlpha - minAlpha)) + minAlpha
        val alpha = min(maxAlpha, max(minAlpha, progressAlpha.roundToInt()))
        drawable.alpha = alpha
        drawable.invalidateSelf()
    }

    private class BackgroundDrawable : ColorDrawable()

    private class ActivityDelegate(
        private val activity: ComponentActivity,
        private val backgroundHelper: ThemeHelper
    ) : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            val started = backgroundHelper.isStarted
            if (event.targetState.isAtLeast(Lifecycle.State.STARTED)) {
                if (!started) {
                    backgroundHelper.onStart(activity)
                }
            } else {
                if (started) {
                    backgroundHelper.onStop()
                }
            }
        }
    }

    fun interface OnThemeChangeCallback {
        fun onThemeChanged(theme: ColorScheme, isDark: Boolean)
    }

}