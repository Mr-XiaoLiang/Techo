package com.lollipop.lqrdemo.preview.renderer

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.Clipboard
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.databinding.BarcodePreviewContactItemBinding
import com.lollipop.lqrdemo.preview.BarcodePreviewRenderer
import com.lollipop.pigment.Pigment
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.qr.comm.BarcodeWrapper

class ContactViewRenderer : BarcodePreviewRenderer {

    private var recyclerView: RecyclerView? = null
    private val itemList = ArrayList<ItemInfo>()
    private val itemAdapter = ItemAdapter(itemList)

    override fun getView(container: ViewGroup): View {
        val contentView = recyclerView
        if (contentView == null) {
            val context = container.context
            val newView = RecyclerView(context)
            recyclerView = newView
            newView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            newView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            newView.adapter = itemAdapter
            return newView
        } else {
            return contentView
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun render(barcode: BarcodeWrapper) {
        clearItem()
        val info = barcode.info
        when (info) {
            is BarcodeInfo.Contact -> {
                addItemNotEmpty(createItem(info.name))
                addItemNotEmpty(crateItemOrganization(info.organization))
                addItemNotEmpty(crateItemOrganization(info.title))
                for (address in info.addresses) {
                    addItemNotEmpty(createItem(address))
                }
                for (phone in info.phones) {
                    addItemNotEmpty(createItem(phone))
                }
                for (email in info.emails) {
                    addItemNotEmpty(createItem(email))
                }
                for (url in info.urls) {
                    addItemNotEmpty(crateItemUrl(url))
                }
            }

            is BarcodeInfo.Phone -> {
                addItemNotEmpty(createItem(info))
            }

            is BarcodeInfo.Email -> {
                addItemNotEmpty(createItem(info))
            }

            else -> {}
        }
        itemAdapter.notifyDataSetChanged()
    }

    override fun onDecorationChanged(pigment: Pigment) {
        itemAdapter.onDecorationChanged(pigment)
    }

    private fun clearItem() {
        itemList.clear()
    }

    private fun addItemNotEmpty(item: ItemInfo) {
        if (item.value.isNotEmpty()) {
            itemList.add(item)
        }
    }

    private fun crateItemOrganization(organization: String): ItemInfo {
        return ItemInfo(R.drawable.ic_baseline_work_24, organization)
    }

    private fun crateItemUrl(url: String): ItemInfo {
        return ItemInfo(R.drawable.ic_baseline_web_24, url)
    }

    private fun createItem(person: BarcodeInfo.PersonName): ItemInfo {
        return ItemInfo(R.drawable.ic_baseline_person_24, person.getDisplayValue())
    }

    private fun createItem(address: BarcodeInfo.Address): ItemInfo {
        val icon = when (address.type) {
            BarcodeInfo.Address.Type.UNKNOWN -> {
                R.drawable.ic_baseline_location_on_24
            }

            BarcodeInfo.Address.Type.WORK -> {
                R.drawable.ic_baseline_work_24
            }

            BarcodeInfo.Address.Type.HOME -> {
                R.drawable.ic_baseline_home_24
            }
        }
        return ItemInfo(icon, address.lines.joinToString(separator = "\n"))
    }

    private fun createItem(phone: BarcodeInfo.Phone): ItemInfo {
        val icon = when (phone.type) {
            BarcodeInfo.Phone.Type.UNKNOWN -> {
                R.drawable.ic_baseline_phone_24
            }

            BarcodeInfo.Phone.Type.WORK -> {
                R.drawable.ic_baseline_work_24
            }

            BarcodeInfo.Phone.Type.HOME -> {
                R.drawable.ic_baseline_home_24
            }

            BarcodeInfo.Phone.Type.FAX -> {
                R.drawable.ic_baseline_fax_24
            }

            BarcodeInfo.Phone.Type.MOBILE -> {
                R.drawable.ic_baseline_phone_android_24
            }
        }
        return ItemInfo(icon, phone.number)
    }

    private fun createItem(email: BarcodeInfo.Email): ItemInfo {
        val icon = when (email.type) {
            BarcodeInfo.Email.Type.UNKNOWN -> {
                R.drawable.ic_baseline_email_24
            }

            BarcodeInfo.Email.Type.WORK -> {
                R.drawable.ic_baseline_work_24
            }

            BarcodeInfo.Email.Type.HOME -> {
                R.drawable.ic_baseline_home_24
            }
        }
        return ItemInfo(icon, email.address)
    }

    private class ItemAdapter(
        private val itemList: List<ItemInfo>
    ) : RecyclerView.Adapter<ItemViewHolder>() {

        private var pigment: Pigment? = null
        private var layoutInflater: LayoutInflater? = null

        private fun getLayoutInflater(parent: ViewGroup): LayoutInflater {
            if (layoutInflater == null) {
                layoutInflater = LayoutInflater.from(parent.context)
            }
            return layoutInflater!!
        }

        @SuppressLint("NotifyDataSetChanged")
        fun onDecorationChanged(newPigment: Pigment) {
            this.pigment = newPigment
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ItemViewHolder {
            return ItemViewHolder(
                BarcodePreviewContactItemBinding.inflate(
                    getLayoutInflater(parent),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(
            holder: ItemViewHolder,
            position: Int
        ) {
            holder.bind(itemList[position], pigment)
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

    }

    private class ItemViewHolder(
        val binding: BarcodePreviewContactItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.copyButton.setOnClickListener {
                onCopyClick()
            }
        }

        private fun onCopyClick() {
            binding.contentInfoView.text?.toString()?.let {
                val context = itemView.context
                Clipboard.copy(context, value = it)
                Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
            }
        }

        fun bind(item: ItemInfo, pigment: Pigment?) {
            binding.contactTypeIcon.setImageResource(item.icon)
            binding.contentInfoView.text = item.value
            pigment?.let {
                binding.contentInfoView.setTextColor(it.onBackgroundTitle)
                binding.copyButton.imageTintList = ColorStateList.valueOf(it.onBackgroundBody)
                binding.contactTypeIcon.imageTintList = ColorStateList.valueOf(it.onBackgroundBody)
            }
        }

    }

    private class ItemInfo(
        val icon: Int,
        val value: String
    )

}