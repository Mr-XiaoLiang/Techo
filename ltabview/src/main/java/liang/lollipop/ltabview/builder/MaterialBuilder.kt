package liang.lollipop.ltabview.builder

import liang.lollipop.ltabview.LTabView
import liang.lollipop.ltabview.item.MaterialItem

/**
 * @date: 2019-04-20 15:34
 * @author: lollipop
 * MaterialItem的构造方法
 */
class MaterialBuilder(view: LTabView): BaseBuilder(view) {

    fun addItem(block: MaterialItem.() -> Unit): MaterialBuilder {
        tabView.addView(MaterialItem(tabView.context).apply(block))
        return this
    }

}