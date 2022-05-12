package com.lollipop.techo.view

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.lollipop.base.request.PermissionFlow
import com.lollipop.techo.R

object PermissionFeedbackDialog {

    private val permissionMap = hashMapOf(
        Manifest.permission.READ_EXTERNAL_STORAGE to R.string.permission_rationale_read_external_storage,
    )

    private fun show(context: Context, message: Int, feedback: PermissionFlow.PermissionFeedback) {
        AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(R.string.granted) { dialog, _ ->
                feedback.onFeedback(true)
                dialog.dismiss()
            }.setNegativeButton(R.string.denied) { dialog, _ ->
                feedback.onFeedback(false)
                dialog.dismiss()
            }.show()
    }

    fun showByPermission(
        context: Context,
        permissions: String,
        feedback: PermissionFlow.PermissionFeedback
    ) {
        val message = permissionMap[permissions] ?: 0
        if (message == 0) {
            return feedback.onFeedback(false)
        }
        show(context, message, feedback)
    }

}