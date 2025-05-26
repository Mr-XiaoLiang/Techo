package com.lollipop.lqrdemo

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.base.util.lazyBind
import com.lollipop.insets.WindowInsetsEdge
import com.lollipop.insets.fixInsetsByPadding
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.databinding.FragmentMainDrawerBinding
import com.lollipop.lqrdemo.databinding.ItemMainDrawerBinding
import com.lollipop.lqrdemo.floating.FloatingScanSettingsActivity
import com.lollipop.lqrdemo.other.AboutActivity
import com.lollipop.lqrdemo.other.PrivacyAgreementActivity
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentWallpaperCenter

class MainDrawerFragment : BaseFragment() {

    private val binding: FragmentMainDrawerBinding by lazyBind()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(
            view.context, RecyclerView.VERTICAL, false
        )
        binding.recyclerView.adapter = ItemAdapter(getItemList())
        binding.root.fixInsetsByPadding(WindowInsetsEdge.ALL)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.root.setBackgroundColor(pigment.backgroundColor)
        binding.recyclerView.adapter?.notifyDataSetChanged()
        binding.logoImageView.setBackgroundColor(pigment.secondaryColor)
        binding.logoImageView.imageTintList = ColorStateList.valueOf(pigment.onSecondaryTitle)
    }

    private fun getItemList(): List<Item> {
        return listOf(
            Item(R.string.title_privacy_agreement) {
                context?.let { c ->
                    startActivity(Intent(c, PrivacyAgreementActivity::class.java))
                }
            },
            Item(R.string.title_floating_scan_settings) {
                context?.let { c ->
                    startActivity(Intent(c, FloatingScanSettingsActivity::class.java))
                }
            },
            Item(R.string.title_about) {
                context?.let { c ->
                    startActivity(Intent(c, AboutActivity::class.java))
                }
            }
        )
    }

    private class ItemAdapter(
        private val itemList: List<Item>,
    ) : RecyclerView.Adapter<ItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder(parent.bind(false))
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            holder.bind(itemList[position])
        }

    }


    private class ItemHolder(
        private val binding: ItemMainDrawerBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            PigmentWallpaperCenter.pigment?.let {
                binding.itemTextView.setTextColor(it.onBackgroundTitle)
            }
            binding.itemTextView.setText(item.value)
            binding.itemTextView.setOnClickListener {
                item.onClickCallback()
            }
        }
    }


    private class Item(
        @StringRes
        val value: Int,
        val onClickCallback: () -> Unit,
    )

}