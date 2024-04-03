package com.lollipop.punch2.activity

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ColorScheme
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.WindowInsetsEdgeStrategy
import com.lollipop.insets.WindowInsetsHelper
import com.lollipop.insets.fixInsetsByMargin
import com.lollipop.punch2.data.FlagInfo
import com.lollipop.punch2.databinding.ActivityMainBinding
import com.lollipop.punch2.list.PunchStampHolder
import com.lollipop.punch2.list.delegate.HomeListDelegate
import com.lollipop.punch2.utils.ThemeHelper
import com.lollipop.punch2.utils.bind
import com.lollipop.punch2.utils.lazyBind

class MainActivity : AppCompatActivity(), HomeListDelegate.Callback {

    private val binding: ActivityMainBinding by lazyBind()
    private val themeHelper = ThemeHelper.createWith(this) { theme, dark ->
        onThemeChanged(theme, dark)
    }

    private val delegate = HomeListDelegate.create(this, this)

    private val adapter = Adapter(delegate.data, delegate::punchFlag)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        themeHelper.bindView(binding.headerBackgroundView)
        binding.statusGuideline.fixInsetsByMargin(WindowInsetsEdge.HEADER)
        binding.pieChartButton.fixInsetsByMargin(WindowInsetsEdge.HEADER.baseTo(top = WindowInsetsEdgeStrategy.ORIGINAL))
        binding.addButton.fixInsetsByMargin(WindowInsetsEdge.HEADER.baseTo(top = WindowInsetsEdgeStrategy.ORIGINAL))
        binding.recyclerView.let {
            it.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
            it.adapter = adapter
        }

    }

    private fun onThemeChanged(theme: ColorScheme, isDark: Boolean) {
        WindowInsetsHelper.getController(this).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
        if (isDark) {
            updateActionBarIcon(ColorStateList.valueOf(Color.WHITE))
            binding.dayView.setTextColor(Color.WHITE)
            binding.recyclerView.setBackgroundColor(Color.BLACK)
        } else {
            updateActionBarIcon(ColorStateList.valueOf(Color.BLACK))
            binding.dayView.setTextColor(Color.BLACK)
            binding.recyclerView.setBackgroundColor(Color.WHITE)
        }
    }

    private fun updateActionBarIcon(iconColor: ColorStateList) {
        binding.pieChartButton.imageTintList = iconColor
        binding.calendarButton.imageTintList = iconColor
        binding.addButton.imageTintList = iconColor
        binding.tabCompletedButton.imageTintList = iconColor
        binding.tabIncompleteButton.imageTintList = iconColor
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDelegateCallback(result: HomeListDelegate.OpResult) {
        when (result) {
            is HomeListDelegate.OpResult.Error -> {
                val errorType = result.type
                when (errorType) {
                    HomeListDelegate.ErrorType.LIST_ALL -> TODO()
                    HomeListDelegate.ErrorType.LIST_MORE -> TODO()
                    HomeListDelegate.ErrorType.PUNCH -> TODO()
                    HomeListDelegate.ErrorType.REMOVE -> TODO()
                }
            }

            HomeListDelegate.OpResult.FullUpdate -> {
                adapter.notifyDataSetChanged()
            }

            is HomeListDelegate.OpResult.ItemRemoved -> {
                adapter.notifyItemRangeRemoved(result.position, adapter.itemCount)
            }

            HomeListDelegate.OpResult.Loading -> {
                // TODO
            }
        }
    }

    private class Adapter(
        val data: List<FlagInfo>,
        private val onPunchClick: (Int) -> Unit
    ) : RecyclerView.Adapter<PunchStampHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PunchStampHolder {
            return PunchStampHolder(parent.bind(false), onPunchClick)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: PunchStampHolder, position: Int) {
            holder.bind(data[position])
        }

    }

}