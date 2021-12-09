package com.lollipop.techo.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.data.TechoItemType
import com.lollipop.techo.databinding.ActivityTechoEditBinding
import com.lollipop.techo.databinding.ActivityTechoEditFloatingBinding
import com.lollipop.techo.util.CircleAnimationGroup
import com.lollipop.techo.view.CheckableView

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
        CircleAnimationGroup<FloatingActionButton>()
    }

    private var quickAddType = TechoItemType.Text

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        floatingView.fixInsetsByPadding(WindowInsetsHelper.Edge.ALL)
        initMenuBtn()

        viewBinding.leftCheckBox.setStyle(CheckableView.CheckStyle.CIRCULAR)
        viewBinding.rightCheckBox.setStyle(CheckableView.CheckStyle.SQUARE)
    }

    private fun initMenuBtn() {
        circleAnimationGroup.setCenterView(floatingBinding.floatingMenuBtn)
        circleAnimationGroup.addPlanet(
            floatingBinding.floatingTextBtn to true,
            floatingBinding.floatingNumberBtn to true,
            floatingBinding.floatingCheckboxBtn to true,
            floatingBinding.floatingPhotoBtn to true,
            floatingBinding.floatingSplitBtn to true,
            floatingBinding.floatingTest6 to false,
            floatingBinding.floatingTest7 to false,
            floatingBinding.floatingTest8 to false,
            floatingBinding.floatingTest9 to false,
            floatingBinding.floatingTest10 to false,
            floatingBinding.floatingTest11 to false,
            floatingBinding.floatingTest12 to false,
            floatingBinding.floatingTest13 to false,
            floatingBinding.floatingTest14 to false,
        )
        circleAnimationGroup.onPlanetClick(::onFloatBtnClick)
        circleAnimationGroup.bindListener {
            onProgressUpdate { progress ->
                floatingBinding.floatingMenuBtn.rotation = progress * 135
            }
            onHideCalled {
                floatingBinding.quickAddButton.show()
            }
            onShowCalled {
                floatingBinding.quickAddButton.hide()
            }
        }
        floatingBinding.quickAddButton.setOnClickListener {
            addItemByType()
        }
        floatingBinding.floatingMenuBtn.setOnClickListener {
            if (circleAnimationGroup.isOpened) {
                circleAnimationGroup.close()
            } else {
                circleAnimationGroup.open()
            }
        }

        // 初始化默认的类型
        quickAddType = TechoItemType.Text
        floatingBinding.quickAddButton.setImageDrawable(floatingBinding.floatingTextBtn.drawable)
        circleAnimationGroup.hide()
    }

    private fun onFloatBtnClick(btn: FloatingActionButton) {
        floatingBinding.quickAddButton.setImageDrawable(btn.drawable)
        when (btn) {
            floatingBinding.floatingTextBtn -> {
                quickAddType = TechoItemType.Text
            }
            floatingBinding.floatingNumberBtn -> {
                quickAddType = TechoItemType.Number
            }
            floatingBinding.floatingCheckboxBtn -> {
                quickAddType = TechoItemType.CheckBox
            }
            floatingBinding.floatingPhotoBtn -> {
                quickAddType = TechoItemType.Photo
            }
            floatingBinding.floatingSplitBtn -> {
                quickAddType = TechoItemType.Split
            }
        }
        circleAnimationGroup.close()
        addItemByType()
    }

    private fun addItemByType() {
        Toast.makeText(this, "添加了$quickAddType", Toast.LENGTH_SHORT).show()
        // TODO
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