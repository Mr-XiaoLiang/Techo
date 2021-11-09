package com.lollipop.techo.activity

import android.os.Bundle
import android.view.View
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityTechoEditBinding
import com.lollipop.techo.databinding.ActivityTechoEditFloatingBinding
import com.lollipop.techo.util.CircleAnimationGroup

/**
 * 编辑 & 添加页
 */
class TechoEditActivity : HeaderActivity() {

    private val viewBinding: ActivityTechoEditBinding by lazyBind()

    private val floatingBinding: ActivityTechoEditFloatingBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root

    override val floatingView: View
        get() = floatingBinding.root

    private val circleAnimationGroup by lazy {
        CircleAnimationGroup()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        floatingView.fixInsetsByPadding(WindowInsetsHelper.Edge.ALL)
        initMenuBtn()
    }

    private fun initMenuBtn() {
        circleAnimationGroup.setCenterView(floatingBinding.floatingMenuBtn)
        circleAnimationGroup.addPlanet(
            floatingBinding.floatingTest1,
            floatingBinding.floatingTest2,
            floatingBinding.floatingTest3,
            floatingBinding.floatingTest4,
            floatingBinding.floatingTest5,
            floatingBinding.floatingTest6,
            floatingBinding.floatingTest7,
            floatingBinding.floatingTest8,
            floatingBinding.floatingTest9,
            floatingBinding.floatingTest10,
            floatingBinding.floatingTest11,
            floatingBinding.floatingTest12,
            floatingBinding.floatingTest13,
            floatingBinding.floatingTest14,
        )
        circleAnimationGroup.hide()
        circleAnimationGroup.onProgressUpdate { progress ->
            floatingBinding.floatingMenuBtn.rotation = progress * 135
        }
        floatingBinding.floatingMenuBtn.setOnClickListener {
            if (circleAnimationGroup.isOpened) {
                circleAnimationGroup.close()
            } else {
                circleAnimationGroup.open()
            }
        }
    }

    override fun onBackPressed() {
        if (circleAnimationGroup.isOpened) {
            circleAnimationGroup.close()
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        circleAnimationGroup.destroy()
    }

}