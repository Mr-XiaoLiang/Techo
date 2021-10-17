package com.lollipop.techo.activity

import android.view.View
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityTechoEditBinding

class TechoEditActivity : HeaderActivity() {

    private val viewBinding: ActivityTechoEditBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root


}