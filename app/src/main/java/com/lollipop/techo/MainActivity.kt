package com.lollipop.techo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.lollipop.base.request.PermissionCallback
import com.lollipop.base.request.startPermissionFlow
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onUI
import com.lollipop.gallery.PhotoManager
import com.lollipop.techo.databinding.ActivityMainBinding

class MainActivity : HeaderActivity() {

    private val viewBinding: ActivityMainBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root

    private val photoManager = PhotoManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startPermissionFlow()
            .permissionIs(PhotoManager.READ_PERMISSION)
            .request {
                doAsync {
                    photoManager.refresh(this)
                    val photoSize = photoManager.size
                    onUI {
                        Toast.makeText(this, "获取到${photoSize}张照片", Toast.LENGTH_SHORT).show()
                    }
                }
            }

    }

}