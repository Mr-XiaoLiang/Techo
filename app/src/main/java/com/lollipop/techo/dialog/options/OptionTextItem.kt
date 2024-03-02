package com.lollipop.techo.dialog.options

import android.view.View
import android.widget.TextView
import com.lollipop.base.util.onClick
import com.lollipop.brackets.core.TypedProvider
import com.lollipop.brackets.core.TypedResponse
import com.lollipop.brackets.impl.XmlBrackets
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoTheme
import com.lollipop.techo.dialog.OptionMenuDialog

class OptionTextItem(
    protocol: OptionTextItemProtocol
) : XmlBrackets<OptionTextItemProtocol>(protocol) {
    override val layoutId: Int
        get() = R.layout.dialog_option_brackets_text_item

    override fun bindView(view: View) {
        view.child<TextView>(R.id.itemTextView) {
            it.text = protocol.title()
            it.setTextColor(protocol.theme().onBackgroundTitle)
            it.onClick {
                protocol.onClick(tag)
            }
        }
    }

}

class OptionTextItemProtocol(
    theme: TypedProvider<TechoTheme.Snapshot>
) : OptionMenuDialog.OptionProtocol(theme) {

    var onClick: TypedResponse<String> = TypedResponse {}

}

inline fun OptionMenuDialog.OptionScope.Item(
    builder: (OptionTextItemProtocol).() -> Unit
) {
    val protocol = OptionTextItemProtocol(themeProvider)
    builder(protocol)
    add(OptionTextItem(protocol))
}