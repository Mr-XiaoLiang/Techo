package com.lollipop.fragment

import androidx.fragment.app.Fragment

interface FragmentInfo {
    val fragment: Class<out Fragment>
    val tag: String
}

data class SimpleFragmentInfo(
    override val fragment: Class<out Fragment>,
    override val tag: String
) : FragmentInfo
