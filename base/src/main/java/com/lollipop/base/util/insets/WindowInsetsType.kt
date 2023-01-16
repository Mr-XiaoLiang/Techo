package com.lollipop.base.util.insets

/**
 * 这是WindowInsetsCompat.Type的枚举抽象，以此来限制代码取值范围
 * 所有注释来源于WindowInsetsCompat，详情请参见WindowInsetsCompat.Type
 */
enum class WindowInsetsType {
    /**
     * @return All system bars. Includes {@link #statusBars()}, {@link #captionBar()} as well as
     * {@link #navigationBars()}, but not {@link #ime()}.
     */
    SYSTEM_BARS,

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
    DISPLAY_CUTOUT,

    /**
     * @see #getTappableElementInsets
     */
    TAPPABLE_ELEMENT,

    /**
     * @see #getMandatorySystemGestureInsets
     */
    MANDATORY_SYSTEM_GESTURES,

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
    SYSTEM_GESTURES,

    /**
     * @return An insets type representing the window of an {@link InputMethod}.
     */
    IME,

    /**
     * @return An insets type representing the window of a caption bar.
     */
    CAPTION_BAR,

    /**
     * @return An insets type representing any system bars for navigation.
     */
    NAVIGATION_BARS,

    /**
     * @return An insets type representing any system bars for displaying status.
     */
    STATUS_BARS
}