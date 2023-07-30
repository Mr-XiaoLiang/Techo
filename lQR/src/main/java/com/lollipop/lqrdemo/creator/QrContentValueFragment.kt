package com.lollipop.lqrdemo.creator

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.checkCallback
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.fixInsetsByMargin
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.creator.bridge.OnCodeContentChangedListener
import com.lollipop.lqrdemo.databinding.FragmentQrContentValueBinding
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment

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
        onUI {
            binding.inputEditView.text = value
        }
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.contentGroup.setBackgroundColor(pigment.extreme)
        BlendMode.flow(pigment.primaryColor)
            .blend(pigment.extreme, 0.7F) {
                binding.inputEditView.setBackgroundColor(it)
            }.content {
                binding.inputEditView.setTextColor(it)
            }
    }

    override fun onResume() {
        super.onResume()
        binding.inputEditView.text = callback?.getQrContentInfo() ?: ""
    }

    interface Callback {
        fun getQrContentInfo(): String

        fun requestChangeContent()
    }

}