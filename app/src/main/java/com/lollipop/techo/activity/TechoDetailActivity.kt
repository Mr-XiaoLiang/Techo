package com.lollipop.techo.activity

import android.view.View
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityTechoDetailBinding

/**
 * 详情页
 */
class TechoDetailActivity : HeaderActivity() {

    private val viewBinding: ActivityTechoDetailBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root

}