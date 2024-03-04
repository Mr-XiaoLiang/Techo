package com.lollipop.techo.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.lazyBind
import com.lollipop.brackets.core.Protocol
import com.lollipop.brackets.core.Scope
import com.lollipop.brackets.core.TypedProvider
import com.lollipop.brackets.framework.BracketsHandler
import com.lollipop.brackets.framework.RecyclerBracketsAdapter
import com.lollipop.techo.data.AppTheme
import com.lollipop.techo.data.TechoTheme
import com.lollipop.techo.databinding.DialogOptionMenuBinding

/**
 * OptionMenu默认上下文选择菜单现在用的是系统自带的，不方便切换样式
 * 还是自己写的好控制一些
 */
class OptionMenuDialog(
    context: Context,
    private val contentBuilder: OptionScope.(OptionMenuDialog) -> Unit
) : BaseBottomSheetDialog(context) {

    private val adapter = RecyclerBracketsAdapter()
    private val binding: DialogOptionMenuBinding by lazyBind()
    private var adapterHandler: BracketsHandler? = null
    private val defaultTheme by lazy {
        TechoTheme.valueOf(AppTheme.current)
    }
    private val optionThemeProvider: TypedProvider<TechoTheme.Snapshot> = TypedProvider {
        dialogTheme ?: defaultTheme
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initRecyclerContainer(context)
    }

    private fun initRecyclerContainer(context: Context) {
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val adapter = RecyclerBracketsAdapter()
        adapterHandler = BracketsHandler(adapter)
        recyclerView.adapter = adapter
        adapterHandler?.build {
            OptionScope(this, optionThemeProvider).contentBuilder(this@OptionMenuDialog)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDecorationChanged(snapshot: TechoTheme.Snapshot) {
        super.onDecorationChanged(snapshot)
        binding.contentPanel.setBackgroundColor(snapshot.backgroundColor)
        binding.sheetBar.color = snapshot.primaryVariant
        // 更新主题
        adapter.notifyDataSetChanged()
    }

    class OptionScope(
        private val rootScope: Scope,
        val themeProvider: TypedProvider<TechoTheme.Snapshot>
    ) : Scope by rootScope

    open class OptionProtocol(val theme: TypedProvider<TechoTheme.Snapshot>) : Protocol()

}