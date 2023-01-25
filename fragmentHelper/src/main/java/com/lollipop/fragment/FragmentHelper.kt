package com.lollipop.fragment

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

object FragmentHelper {

    fun with(fragmentManager: FragmentManager, container: ViewGroup): FragmentSwitcher {
        return FragmentSwitcher(fragmentManager, container.id)
    }

    fun with(activity: AppCompatActivity, container: ViewGroup): FragmentSwitcher {
        return with(activity.supportFragmentManager, container)
    }

    fun with(fragment: Fragment, container: ViewGroup): FragmentSwitcher {
        return with(fragment.childFragmentManager, container)
    }

}