package com.lollipop.base.util

import android.content.Context
import android.content.Intent

object Sharesheet {

    fun shareText(context: Context, info: String) {
        context.startActivity(
            Intent.createChooser(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, info)
                    type = "text/plain"
                },
                null
            )
        )
    }

    fun shareEmail(context: Context, emailAddress: String, subject: String) {
        context.startActivity(
            Intent.createChooser(
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    type = "text/plain"
                },
                null
            )
        )
    }

}