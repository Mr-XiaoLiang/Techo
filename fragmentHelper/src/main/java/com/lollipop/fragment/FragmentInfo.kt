package com.lollipop.fragment

import androidx.fragment.app.Fragment

interface FragmentInfo {
    val fragment: Class<Fragment>
    val tag: String
}

data class SimpleFragmentInfo(
    override val fragment: Class<Fragment>,
    override val tag: String
) : FragmentInfo
