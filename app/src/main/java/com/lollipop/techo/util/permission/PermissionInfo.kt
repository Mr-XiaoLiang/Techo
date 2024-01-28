package com.lollipop.techo.util.permission

import androidx.annotation.StringRes

class PermissionInfo(
    val permission: Array<String>,
    @StringRes
    val rationale: Int,
    val anyOne: Boolean = true
)