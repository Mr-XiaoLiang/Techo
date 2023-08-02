package com.lollipop.lqrdemo.creator

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.databinding.FragmentQrEditBackgroundBinding

/**
 * 背景的设置
 */
class QrBackgroundFragment : BaseFragment() {

    private val binding: FragmentQrEditBackgroundBinding by lazyBind()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    private var color = Color.GREEN

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.tempButton.onClick {
//            PaletteDialog.show(view.context, color) {
//                color = it
//                Toast.makeText(view.context, it.toString(16), Toast.LENGTH_SHORT).show()
//            }
//        }
        binding.colorModeButton.onClick {
            PaletteDialog.show(view.context, color) {
                color = it
                Toast.makeText(view.context, it.toString(16), Toast.LENGTH_SHORT).show()
            }
        }
    }

}