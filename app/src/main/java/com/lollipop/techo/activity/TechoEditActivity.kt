package com.lollipop.techo.activity

import android.os.Bundle
import android.view.View
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityTechoDetailFloatingBinding
import com.lollipop.techo.databinding.ActivityTechoEditBinding

/**
 * 编辑 & 添加页
 */
class TechoEditActivity : HeaderActivity() {

    private val viewBinding: ActivityTechoEditBinding by lazyBind()

    private val floatingBinding: ActivityTechoDetailFloatingBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root

    override val floatingView: View
        get() = floatingBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        floatingView.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)
    }

}