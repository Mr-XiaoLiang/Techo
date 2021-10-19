package com.lollipop.techo.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.lollipop.base.util.WindowInsetsHelper
import com.lollipop.base.util.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.gallery.PhotoManager
import com.lollipop.guide.GuideHelper
import com.lollipop.guide.GuideStep
import com.lollipop.guide.impl.OvalGuideStep
import com.lollipop.techo.activity.HeaderActivity
import com.lollipop.techo.databinding.ActivityMainBinding

class MainActivity : HeaderActivity() {

    private val viewBinding: ActivityMainBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root

    private val photoManager = PhotoManager()

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

        contentView.fixInsetsByPadding(WindowInsetsHelper.Edge.CONTENT)

        // 敷衍的更新

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

}