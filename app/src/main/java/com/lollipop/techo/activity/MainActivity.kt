package com.lollipop.techo.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.base.util.registerResult
import com.lollipop.brackets.core.Stateless
import com.lollipop.brackets.core.TypedResponse
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.fixInsetsByPadding
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.tint
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoMode
import com.lollipop.techo.data.TechoMode.ChangedType.Delete
import com.lollipop.techo.data.TechoMode.ChangedType.Full
import com.lollipop.techo.data.TechoMode.ChangedType.Insert
import com.lollipop.techo.data.TechoMode.ChangedType.Modify
import com.lollipop.techo.data.TechoMode.ChangedType.Move
import com.lollipop.techo.databinding.ActivityMainFloatingBinding
import com.lollipop.techo.dialog.OptionMenuDialog
import com.lollipop.techo.dialog.options.ClickWithDismiss
import com.lollipop.techo.dialog.options.Item
import com.lollipop.techo.list.home.HomeListAdapter

class MainActivity : BasicListActivity(),
    TechoMode.ListStateListener {

    private val floatingBinding: ActivityMainFloatingBinding by lazyBind()

    override val showBackArrow = false

    override val floatingView: View
        get() = floatingBinding.root

    override val optionsMenu = true

    private val mode by lazy {
        TechoMode.list(this).bind(this.lifecycle).attach(this).build()
    }

    private val editPageLauncher = registerResult(TechoDetailActivity.LAUNCHER) { newId ->
        if (newId != TechoDetailActivity.NO_ID) {
            mode.onNewInsert(newId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        initRecyclerView {
            it.adapter = HomeListAdapter(mode.info)
            it.layoutManager = LinearLayoutManager(
                this, RecyclerView.VERTICAL, false
            )
        }
        isEnableLoadMore(true)
        isEnableRefresh(true)
        floatingBinding.newTechoBtn.onClick {
            editPageLauncher.launch(null)
//            requestActivity<RecorderActivity> {
//                if (it.isOk) {
//                    Toast.makeText(
//                        this,
//                        RecorderActivity.getAudioFile(it.data),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
        }

        floatingBinding.root.fixInsetsByPadding(WindowInsetsEdge.ALL)
    }

    override fun onLoadStart() {
        if (isRefreshing) {
            return
        }
        showLoading()
    }

    override fun onLoadEnd() {
        isRefreshing = false
        hideLoading()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onInfoChanged(start: Int, count: Int, type: TechoMode.ChangedType) {
        val adapter = recyclerView.adapter ?: return
        when (type) {
            Full -> {
                adapter.notifyDataSetChanged()
            }

            Modify -> {
                adapter.notifyItemRangeChanged(start, count)
            }

            Insert -> {
                adapter.notifyItemRangeInserted(start, count)
            }

            Delete -> {
                adapter.notifyItemRangeRemoved(start, count)
            }

            Move -> {
                adapter.notifyItemMoved(start, count)
            }
        }
        isMoreLoading = false
    }

    override fun onRefresh() {
        mode.refresh()
    }

    override fun onLoadMore() {
        mode.loadNext()
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        floatingBinding.newTechoBtn.tint(pigment)
    }

    override fun OptionMenuDialog.OptionScope.onCreateOptionsMenu(dialog: OptionMenuDialog) {
        Item {
            title = Stateless(getString(R.string.settings))
            ClickWithDismiss(dialog) {
                Toast.makeText(this@MainActivity, "Setting", Toast.LENGTH_SHORT).show()
            }
        }
    }

}