package com.lollipop.techo.edit.impl

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.lollipop.base.util.bind
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.tintByNotObvious
import com.lollipop.pigment.tintBySelectState
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.databinding.PanelTextOptionBinding
import com.lollipop.techo.edit.base.BottomEditDelegate

open class BaseOptionDelegate<T : TechoItem> : BottomEditDelegate<T>() {

    private var binding: PanelTextOptionBinding? = null

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
        val newBinding: PanelTextOptionBinding = container.bind(false)
        binding = newBinding
        return newBinding.root
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        binding?.apply {
            clickToClose(backgroundView)
        }
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding?.doneBtn?.tintByNotObvious(pigment)
        binding?.optionLinearLayout?.let { group ->
            val childCount = group.childCount
            val optionColor = 0xFF333333.toInt()
            for (index in 0 until childCount) {
                group.getChildAt(index)?.let { child ->
                    if (child.id != R.id.colorOptionBtn && child is ImageView) {
                        child.tintBySelectState(pigment, optionColor)
                    }
                }
            }
        }
        binding?.scrollBar?.color = pigment.secondary
    }

}

class TextOptionDelegate : BaseOptionDelegate<TechoItem.Text>()
class TitleOptionDelegate : BaseOptionDelegate<TechoItem.Title>()
class CheckBoxOptionDelegate : BaseOptionDelegate<TechoItem.CheckBox>()
class NumberOptionDelegate : BaseOptionDelegate<TechoItem.Number>()