package com.lollipop.techo.util.permission

import androidx.annotation.StringRes

data class PermissionInfo(
    val permission: String,
    @StringRes
    val rationale: Int
) {
}