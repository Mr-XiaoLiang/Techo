package com.lollipop.lqrdemo.floating

sealed class ScreenshotResult {


    class Success(val url: String) : ScreenshotResult()

    class Failure(val error: Throwable) : ScreenshotResult()

    data object NoAvailable: ScreenshotResult()

    data object UnknownError: ScreenshotResult()

}