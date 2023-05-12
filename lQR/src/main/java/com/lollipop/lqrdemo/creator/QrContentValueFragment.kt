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
import com.lollipop.lqrdemo.databinding.FragmentQrContentValueBinding

class QrContentValueFragment : BaseFragment() {

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
        val c = context ?: return
        QrContentInputPopupWindow.show(c, binding.inputEditView.text ?: "", ::onInputResult)
    }

    private fun onInputResult(value: CharSequence) {
        binding.inputEditView.text = value
        callback?.onQrContentValueChanged(value)
    }

    fun interface Callback {
        fun onQrContentValueChanged(value: CharSequence)
    }

}