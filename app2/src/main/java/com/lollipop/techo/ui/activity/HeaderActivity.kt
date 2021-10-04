package com.lollipop.techo.ui.activity

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.CollapsingToolbarScope
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState

/**
 * @author lollipop
 * @date 2021/10/3 21:04
 */
abstract class HeaderActivity: BaseActivity() {

    @Composable
    override fun Content() {
        CollapsingToolbarScaffold(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            state = rememberCollapsingToolbarScaffoldState(),
            scrollStrategy = ScrollStrategy.EnterAlways,
            toolbar = {
                initHeader()
            }
        ) {
            initContent()
        }
    }

    /**
     * 初始化头部
     */
    @Composable
    open fun CollapsingToolbarScope.initHeader() {
        // 临时头部的代码
        Text(
            text = "Title",
            modifier = Modifier.road(
                    whenCollapsed = Alignment.CenterStart,
                    whenExpanded = Alignment.BottomEnd
                )
        )
    }

    @Composable
    abstract fun initContent()

}