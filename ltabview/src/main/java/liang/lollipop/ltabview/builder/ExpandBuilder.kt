package liang.lollipop.ltabview.builder

import liang.lollipop.ltabview.LTabView
import liang.lollipop.ltabview.item.ExpandItem

/**
 * @date: 2019-04-20 00:06
 * @author: lollipop
 * 横向展开的Item样式专用的构造器
 */
class ExpandBuilder(view: LTabView): BaseBuilder(view) {

    fun addItem(block: ExpandItem.() -> Unit): ExpandBuilder {
        tabView.addView(ExpandItem(tabView.context).apply(block))
        return this
    }

}