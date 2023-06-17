package com.lollipop.lqrdemo.creator

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.checkCallback
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.creator.bridge.OnCodeContentChangedListener
import com.lollipop.lqrdemo.databinding.FragmentQrContentValueBinding

class QrContentValueFragment : BaseFragment(), OnCodeContentChangedListener {

    private val binding: FragmentQrContentValueBinding by lazyBind()

    private var callback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = checkCallback(context)
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.inputEditView.onClick {
            openInputDialog()
        }
    }

    private fun openInputDialog() {
        callback?.requestChangeContent()
    }

    override fun onCodeContentChanged(value: String) {
        TODO("Not yet implemented")
    }

    interface Callback {
        fun getQrContentInfo(): String

        fun requestChangeContent()
    }

}