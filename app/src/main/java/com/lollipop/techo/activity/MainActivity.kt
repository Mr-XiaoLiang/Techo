package com.lollipop.techo.activity

import android.os.Bundle
import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.data.TechoMode
import com.lollipop.techo.databinding.ActivityMainBinding

class MainActivity : HeaderActivity(),
    SwipeRefreshLayout.OnRefreshListener,
    TechoMode.StateListener {

    private val viewBinding: ActivityMainBinding by lazyBind()

    override val contentView: View
        get() = viewBinding.root

    private val mode by lazy {
        TechoMode.create(this).attach(this).buildListMode()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        viewBinding.swipeRefreshLayout.setOnRefreshListener(this)
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

    override fun onRefresh() {
        TODO("Not yet implemented")
    }

}