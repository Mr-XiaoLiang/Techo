package com.lollipop.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.lollipop.base.util.ListenerManager

internal class FragmentCreator {

    private val createdCallback = ListenerManager<FragmentCreatedCallback>()

    fun create(fragmentInfo: FragmentInfo, arguments: Bundle?): Fragment {
        val newInstance = fragmentInfo.fragment.getDeclaredConstructor().newInstance()
        val newArguments = Bundle()
        arguments?.let {
            newArguments.putAll(it)
        }
        newInstance.arguments = newArguments
        onFragmentCreated(newInstance, fragmentInfo)
        return newInstance
    }

    private fun onFragmentCreated(fragment: Fragment, info: FragmentInfo) {
        createdCallback.invoke { it.onFragmentCreated(fragment, info) }
    }

    fun addFragmentCreatedCallback(callback: FragmentCreatedCallback) {
        createdCallback.addListener(callback)
    }

    fun removeFragmentCreatedCallback(callback: FragmentCreatedCallback) {
        createdCallback.removeListener(callback)
    }

}