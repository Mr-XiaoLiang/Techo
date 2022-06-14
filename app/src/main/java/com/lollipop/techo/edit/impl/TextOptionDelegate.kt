package com.lollipop.techo.edit.impl

import android.view.View
import android.view.ViewGroup
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.edit.base.BottomEditDelegate

open class BaseOptionDelegate<T : TechoItem> : BottomEditDelegate<T>() {

    override val contentGroup: View?
        get() = TODO("Not yet implemented")
    override val backgroundView: View?
        get() = TODO("Not yet implemented")

    override fun onCreateView(container: ViewGroup): View {
        TODO("Not yet implemented")
    }

}

class TextOptionDelegate : BaseOptionDelegate<TechoItem.Text>()
class TitleOptionDelegate : BaseOptionDelegate<TechoItem.Title>()
class CheckBoxOptionDelegate : BaseOptionDelegate<TechoItem.CheckBox>()
class NumberOptionDelegate : BaseOptionDelegate<TechoItem.Number>()