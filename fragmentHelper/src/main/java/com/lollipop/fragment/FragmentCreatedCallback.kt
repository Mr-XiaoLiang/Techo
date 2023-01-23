package com.lollipop.fragment

import androidx.fragment.app.Fragment

interface FragmentCreatedCallback {

    fun onFragmentCreated(fragment: Fragment, info: FragmentInfo)

}