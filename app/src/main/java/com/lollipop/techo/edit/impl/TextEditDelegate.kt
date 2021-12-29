package com.lollipop.techo.edit.impl

import android.view.View
import android.view.ViewGroup
import com.lollipop.base.util.bind
import com.lollipop.techo.data.BaseTechoItem
import com.lollipop.techo.data.BaseTextItem
import com.lollipop.techo.databinding.PanelTextEditBinding
import com.lollipop.techo.edit.EditDelegate

/**
 * @author lollipop
 * @date 2021/12/23 22:36
 */
class TextEditDelegate : EditDelegate() {

    private var binding: PanelTextEditBinding? = null

    override val animationEnable = true

    override fun isSupport(info: BaseTechoItem): Boolean {
        return info is BaseTextItem
    }

    override fun onCreateView(container: ViewGroup): View {
        binding?.let {
            return it.root
        }
        val newBinding: PanelTextEditBinding = container.bind(false)
        binding = newBinding
        return newBinding.root
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        binding?.apply {
            doneBtn.setOnClickListener {
                onDone()
            }
            clickToClose(backgroundView)
            keepWithClick(editCard)
        }
    }

    override fun onOpen(info: BaseTechoItem) {
        super.onOpen(info)
        binding?.editText?.setText(
            if (info is BaseTextItem) {
                info.value
            } else {
                ""
            }
        )
    }

    private fun onDone() {
        binding?.editText?.let {
            val newValue = it.text?.toString() ?: return
            tryChangeInfo<BaseTextItem> {
                it.value = newValue
                notifyDataChanged()
            }
        }
        callClose()
    }

    override fun onAnimationUpdate(progress: Float) {
        super.onAnimationUpdate(progress)
        binding?.apply {
            animationAlpha(progress, backgroundView)
            animationDown(progress, editCard)
        }
    }

}