package liang.lollipop.ltabview

import liang.lollipop.ltabview.builder.ExpandBuilder
import liang.lollipop.ltabview.builder.MaterialBuilder

/**
 * 用于辅助调整TabView的帮助类
 */
object LTabHelper {

    fun withExpandItem(view: LTabView): ExpandBuilder {
        return ExpandBuilder(view)
    }

    fun withMaterialItem(view: LTabView): MaterialBuilder {
        return MaterialBuilder(view)
    }

}