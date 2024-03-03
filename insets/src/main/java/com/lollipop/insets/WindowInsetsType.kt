package com.lollipop.insets

import androidx.core.view.WindowInsetsCompat

/**
 * 这是WindowInsetsCompat.Type的枚举抽象，以此来限制代码取值范围
 * 所有注释来源于WindowInsetsCompat，详情请参见WindowInsetsCompat.Type
 */
sealed class WindowInsetsType {

    abstract fun typeMask(): Int

    /**
     * @return All system bars. Includes {@link #statusBars()}, {@link #captionBar()} as well as
     * {@link #navigationBars()}, but not {@link #ime()}.
     */
    data object SystemBars : WindowInsetsType() {
        override fun typeMask(): Int {
            return WindowInsetsCompat.Type.systemBars()
        }
    }

    /**
     * Returns an insets type representing the area that used by {@link DisplayCutoutCompat}.
     *
     * <p>This is equivalent to the safe insets on {@link #getDisplayCutout()}.</p>
     *
     * @see DisplayCutoutCompat#getSafeInsetLeft()
     * @see DisplayCutoutCompat#getSafeInsetTop()
     * @see DisplayCutoutCompat#getSafeInsetRight()
     * @see DisplayCutoutCompat#getSafeInsetBottom()
     */
    data object DisplayCutout : WindowInsetsType() {
        override fun typeMask(): Int {
            return WindowInsetsCompat.Type.displayCutout()
        }
    }

    /**
     * @see #getTappableElementInsets
     */
    data object TappableElement : WindowInsetsType() {
        override fun typeMask(): Int {
            return WindowInsetsCompat.Type.tappableElement()
        }
    }

    /**
     * @see #getMandatorySystemGestureInsets
     */
    data object MandatorySystemGestures : WindowInsetsType() {
        override fun typeMask(): Int {
            return WindowInsetsCompat.Type.mandatorySystemGestures()
        }
    }

    /**
     * Returns an insets type representing the system gesture insets.
     *
     * <p>The system gesture insets represent the area of a window where system gestures have
     * priority and may consume some or all touch input, e.g. due to the a system bar
     * occupying it, or it being reserved for touch-only gestures.
     *
     * <p>Simple taps are guaranteed to reach the window even within the system gesture insets,
     * as long as they are outside the {@link #getSystemWindowInsets() system window insets}.
     *
     * <p>When {@link View#SYSTEM_UI_FLAG_LAYOUT_STABLE} is requested, an inset will be returned
     * even when the system gestures are inactive due to
     * {@link View#SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN} or
     * {@link View#SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION}.
     *
     * @see #getSystemGestureInsets()
     */
    data object SystemGestures : WindowInsetsType() {
        override fun typeMask(): Int {
            return WindowInsetsCompat.Type.systemGestures()
        }
    }

    /**
     * @return An insets type representing the window of an {@link InputMethod}.
     */
    data object Ime : WindowInsetsType() {
        override fun typeMask(): Int {
            return WindowInsetsCompat.Type.ime()
        }
    }

    /**
     * @return An insets type representing the window of a caption bar.
     */
    data object CaptionBar : WindowInsetsType() {
        override fun typeMask(): Int {
            return WindowInsetsCompat.Type.captionBar()
        }
    }

    /**
     * @return An insets type representing any system bars for navigation.
     */
    data object NavigationBars : WindowInsetsType() {
        override fun typeMask(): Int {
            return WindowInsetsCompat.Type.navigationBars()
        }
    }

    /**
     * @return An insets type representing any system bars for displaying status.
     */
    data object StatusBars : WindowInsetsType() {
        override fun typeMask(): Int {
            return WindowInsetsCompat.Type.statusBars()
        }
    }
}