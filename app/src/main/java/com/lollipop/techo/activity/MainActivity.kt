package com.lollipop.techo.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.lollipop.base.util.*
import com.lollipop.bigboom.BigBoomManager
import com.lollipop.bigboom.PresetExplosive
import com.lollipop.bigboom.item.RectanglePatchesItemProvider
import com.lollipop.gallery.PhotoManager
import com.lollipop.guide.GuideHelper
import com.lollipop.guide.GuideStep
import com.lollipop.guide.impl.OvalGuideStep
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoMode
import com.lollipop.techo.databinding.ActivityMainBinding

class MainActivity : HeaderActivity(), TechoMode.StateListener {

    private val viewBinding: ActivityMainBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root

    private val photoManager = PhotoManager()

    private val mode by lazy {
        TechoMode.create(this).attach(this).buildListMode()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        startPermissionFlow()
//            .permissionIs(PhotoManager.READ_PERMISSION)
//            .request {
//                doAsync {
//                    photoManager.refresh(this)
//                    val photoSize = photoManager.size
//                    onUI {
//                        Toast.makeText(this, "获取到${photoSize}张照片", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }

        val bigBoomManager = BigBoomManager {
            viewBinding.bigBoomView
        }
        viewBinding.bigBoomView.bindItemProvider(
            RectanglePatchesItemProvider().apply {
                selectedColor = ContextCompat.getColor(this@MainActivity, R.color.teal_700)
                selectedTextColor = Color.WHITE
                defaultColor = 0x30333333
                defaultTextColor = 0xFF333333.toInt()
                val dp3 = 3.dp2px
                radius = dp3.toFloat()
                margin.set(dp3, dp3, dp3, dp3)
                padding.set(dp3, dp3, dp3, dp3)
                textSize = 14.sp2px.toFloat()
                minWidth = 20.dp2px
            }
        )
        bigBoomManager.startFlow()
            .putFuel("startPermissionFlow(),permissionIs(PhotoManager.READ_PERMISSION),Toast.makeText(this, \"获取到photoSize张照片\", Toast.LENGTH_SHORT).show()")
            .use(PresetExplosive.CHAR)
            .fire()

        contentView.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)

        viewBinding.fab1.setOnClickListener {
            startActivity(Intent(this, TechoEditActivity::class.java))
        }

        viewBinding.fab2.setOnClickListener {
            startActivity(Intent(this, TechoDetailActivity::class.java))
        }

        viewBinding.fab3.setOnClickListener {
            GuideHelper.with(this)
                .addStep(
                    GuideStep(
                        viewBinding.fab1,
                        "第一个引导内容"
                    )
                ).addStep(
                    OvalGuideStep(
                        viewBinding.fab2,
                        "第二个引导内容"
                    )
                ).addStep(
                    GuideStep(
                        viewBinding.fab3,
                        "第三个引导内容，这是一个描述信息，这是一个描述信息，这是一个描述信息，这是一个描述信息，这是一个描述信息，这是一个描述信息，这是一个描述信息，这是一个描述信息，这是一个描述信息，这是一个描述信息，这是一个描述信息，这是一个描述信息，"
                    )
                ).show()
        }

    }

    override fun onLoadStart() {
        TODO("Not yet implemented")
    }

    override fun onLoadEnd() {
        TODO("Not yet implemented")
    }

    override fun onInfoChanged(start: Int, count: Int, type: TechoMode.ChangedType) {
        TODO("Not yet implemented")
    }

}