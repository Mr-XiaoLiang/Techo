package com.lollipop.techo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.lollipop.base.util.doAsync
import com.lollipop.base.util.lazyBind
import com.lollipop.gallery.PhotoManager
import com.lollipop.techo.databinding.ActivityMainBinding

class MainActivity : HeaderActivity() {

    private val viewBinding: ActivityMainBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root

    private val photoManager = PhotoManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (photoManager.checkPermission(this)) {
            Toast.makeText(this, "已经授权了", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "没有授权", Toast.LENGTH_SHORT).show()
        }
        doAsync {
            photoManager.refresh(this)
            val photoSize = photoManager.size
        }
    }

}