package com.lollipop.techo.edit.impl

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import com.lollipop.base.util.*
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.tintByNotObvious
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.PanelTextEditBinding
import com.lollipop.techo.edit.base.TopEditDelegate

/**
 * @author lollipop
 * @date 2021/12/23 22:36
 */
open class BaseTextEditDelegate<T : TechoItem> : TopEditDelegate<T>() {

    private var binding: PanelTextEditBinding? = null

    override val contentGroup: View?
        get() {
            return binding?.editCard
        }
    override val backgroundView: View?
        get() {
            return binding?.backgroundView
        }

    override fun onCreateView(container: ViewGroup): View {
        binding?.let {
            return it.root
        }
        val newBinding: PanelTextEditBinding = container.bind(false)
        binding = newBinding
        newBinding.editCard.fixInsetsByMargin(WindowInsetsHelper.Edge.ALL)
        return newBinding.root
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        binding?.apply {
            doneBtn.onClick {
                onDone()
            }
            clickToClose(backgroundView)
            keepWithClick(editCard)
            editText.doAfterTextChanged {
                notifyDataChanged()
            }
        }
    }

    override fun onOpen(info: T) {
        super.onOpen(info)
        tryUse(binding?.editText) {
            val value = info.value
            it.setText(value)
            it.requestBoard()
        }
    }

    override fun onClose() {
        super.onClose()
        binding?.editText?.closeBoard()
    }

    private fun onDone() {
        binding?.editText?.let {
            val newValue = it.text?.toString() ?: return
            tryChangeInfo { info ->
                info.value = newValue
                notifyDataChanged()
            }
        }
        callClose()
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding?.doneBtn?.tintByNotObvious(pigment)
    }

}

class TextEditDelegate : BaseTextEditDelegate<TechoItem.Text>()
class TitleEditDelegate : BaseTextEditDelegate<TechoItem.Title>()
class CheckBoxEditDelegate : BaseTextEditDelegate<TechoItem.CheckBox>()
class NumberEditDelegate : BaseTextEditDelegate<TechoItem.Number>()