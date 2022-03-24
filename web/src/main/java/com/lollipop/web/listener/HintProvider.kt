package com.lollipop.web.listener

import com.lollipop.web.IWeb

interface HintProvider {

    fun alert(
        view: IWeb,
        url: String,
        message: String,
        result: AlertResult
    ): Boolean

    fun confirm(
        view: IWeb,
        url: String,
        message: String,
        result: ConfirmResult
    ): Boolean

    fun prompt(
        view: IWeb,
        url: String,
        message: String,
        defaultValue: String,
        result: PromptResult
    ): Boolean

    fun beforeUnload(
        view: IWeb,
        url: String,
        message: String,
        result: ConfirmResult
    ): Boolean

    interface AlertResult {
        fun cancel()
        fun confirm()
    }

    interface ConfirmResult {
        fun cancel()
        fun confirm()
    }

    interface PromptResult {
        fun cancel()
        fun confirm(result: String)
    }

}