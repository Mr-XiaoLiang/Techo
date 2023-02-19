package com.lollipop.browser.main

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.view.ViewManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.browser.databinding.PageMainBinding
import com.lollipop.browser.main.launcher.LauncherHolder
import com.lollipop.browser.main.launcher.LauncherInfo
import com.lollipop.browser.main.launcher.LauncherManager

class MainPageDelegate(
    private val binding: PageMainBinding,
    private val launchCallback: (url: String) -> Unit
) {

    companion object {
        fun inflate(
            container: ViewGroup,
            launchCallback: (url: String) -> Unit
        ): MainPageDelegate {
            return MainPageDelegate(container.bind(true), launchCallback)
        }
    }

    private val dataList = ArrayList<LauncherInfo>()

    private val adapter = LauncherAdapter(
        data = dataList,
        onClickCallback = ::onItemClick,
        onLongClickCallback = ::onItemLongClick,
    )

    private fun onItemClick(position: Int) {
        if (position < 0 || position >= dataList.size) {
            return
        }
        launchCallback(dataList[position].url)
    }

    private fun onItemLongClick(position: Int) {
        // TODO 长按进入编辑状态
    }

    fun resume() {
        LauncherManager.load(binding.root.context, ::onDataLoaded)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onDataLoaded(list: List<LauncherInfo>) {
        dataList.clear()
        dataList.addAll(list)
        adapter.notifyDataSetChanged()
    }

    fun destroy() {
        val root = binding.root
        val parent = root.parent
        if (parent is ViewManager) {
            parent.removeView(root)
        }
    }

    private class LauncherAdapter(
        private val data: List<LauncherInfo>,
        private val onClickCallback: (Int) -> Unit,
        private val onLongClickCallback: (Int) -> Unit,
    ) : RecyclerView.Adapter<LauncherHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LauncherHolder {
            return LauncherHolder.create(
                parent,
                onClickCallback,
                onLongClickCallback,
            )
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: LauncherHolder, position: Int) {
            holder.bind(data[position])
        }

    }

}