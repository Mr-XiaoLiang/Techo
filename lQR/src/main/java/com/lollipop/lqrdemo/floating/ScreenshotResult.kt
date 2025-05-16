package com.lollipop.lqrdemo.floating

sealed class ScreenshotResult {


    class Success(val url: String) : ScreenshotResult() {
        override fun toString(): String {
            return "ScreenshotResult.Success($url)"
        }
    }

    class Failure(val error: Throwable) : ScreenshotResult() {
        override fun toString(): String {
            return "ScreenshotResult.Failure(${error.message})"
        }
    }

    data object NoAvailable: ScreenshotResult() {
        override fun toString(): String {
            return "ScreenshotResult.NoAvailable"
        }
    }

    data object UnknownError: ScreenshotResult() {
        override fun toString(): String {
            return "ScreenshotResult.UnknownError"
        }
    }

}