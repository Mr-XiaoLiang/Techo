package com.lollipop.techo.option

import android.content.Context
import android.content.DialogInterface
import android.graphics.Outline
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lollipop.base.util.bind
import com.lollipop.base.util.findInSelf
import com.lollipop.base.util.onClick
import com.lollipop.techo.R
import com.lollipop.techo.databinding.ItemOptionBinding
import com.lollipop.techo.option.OptionDialog.*
import com.lollipop.techo.option.item.Option
import com.lollipop.techo.option.item.OptionDefault
import com.lollipop.techo.option.share.ShareType
import okhttp3.internal.toImmutableList

/**
 * @author lollipop
 * @date 2021/12/30 20:54
 * 操作对话框，支持分享和常规操作
 */
class OptionDialog(
    private val config: Config
) : BottomSheetDialog(
    config.context,
    config.cancelable,
    CancelListenerAdapter(config.cancelListener)
) {

    companion object {
        fun create(context: Context): Builder {
            return Builder(context)
        }
    }

    private val shareGroup: RecyclerView? by findInSelf()
    private val optionGroup: RecyclerView? by findInSelf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_option)
        val dialog = this
        if (config.showShare) {
            shareGroup?.apply {
                isVisible = true
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = OptionAdapter(createShareList()) { option ->
                    ShareType.findByOptionId(option.id)?.let { type ->
                        config.shareClickListener.onShareClick(dialog, type)
                    }
                }
            }
        } else {
            shareGroup?.isVisible = false
        }
        val optionList = config.optionList
        if (optionList.isNotEmpty()) {
            optionGroup?.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = OptionAdapter(optionList) { option ->
                    config.optionClickListener.onOptionClick(dialog, option)
                }
                isVisible = true
            }
        } else {
            optionGroup?.isVisible = false
        }
    }

    private fun createShareList(): List<Option> {
        return ShareType.values().map { it.option() }
    }

    private class OptionAdapter(
        private val data: List<Option>,
        private val listener: (Option) -> Unit
    ) : RecyclerView.Adapter<OptionHolder>(), OnItemClickListener {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionHolder {
            return OptionHolder.create(parent, this)
        }

        override fun onBindViewHolder(holder: OptionHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onItemClick(holder: OptionHolder) {
            val position = holder.adapterPosition
            if (position in data.indices) {
                listener(data[position])
            }
        }

    }

    private class OptionHolder(
        private val binding: ItemOptionBinding,
        private val onItemClickCallback: OnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun create(group: ViewGroup, listener: OnItemClickListener): OptionHolder {
                return OptionHolder(group.bind(), listener)
            }
        }

        init {
            itemView.onClick {
                onClick()
            }
            binding.iconView.clipToOutline = true
            binding.iconView.outlineProvider = OvalOutlineProvider()
        }

        fun bind(option: Option) {
            binding.iconView.setBackgroundResource(option.background)
            binding.iconView.setImageResource(option.icon)
            binding.nameView.setText(option.name)
        }

        private fun onClick() {
            onItemClickCallback.onItemClick(this)
        }

    }

    private class OvalOutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            view ?: return
            outline ?: return
            outline.setOval(0, 0, view.width, view.height)
        }
    }

    private interface OnItemClickListener {
        fun onItemClick(holder: OptionHolder)
    }

    private class CancelListenerAdapter(
        private val listener: OnCancelListener
    ) : DialogInterface.OnCancelListener {
        override fun onCancel(dialog: DialogInterface?) {
            if (dialog is OptionDialog) {
                listener.onCancel(dialog)
            }
        }
    }

    class Config(
        val context: Context,
        val showShare: Boolean,
        val cancelable: Boolean,
        val optionList: List<Option>,
        val optionClickListener: OnOptionClickListener,
        val shareClickListener: OnShareClickListener,
        val cancelListener: OnCancelListener,
    )

    class Builder(val context: Context) {
        private var showShare = true
        private var cancelable = true
        private val optionList = ArrayList<Option>()
        private var optionClickListener: OnOptionClickListener? = null
        private var shareClickListener: OnShareClickListener? = null
        private val optionFilterList = ArrayList<OptionFilter>()
        private var cancelListener: OnCancelListener? = null

        fun showShare(isShow: Boolean): Builder {
            this.showShare = isShow
            return this
        }

        fun cancelable(enable: Boolean): Builder {
            this.cancelable = enable
            return this
        }

        fun addOption(option: Option): Builder {
            optionList.add(option)
            return this
        }

        fun addOption(option: OptionDefault): Builder {
            return addOption(option.new())
        }

        fun onOptionClick(listener: OnOptionClickListener): Builder {
            this.optionClickListener = listener
            return this
        }

        fun onShareClick(listener: OnShareClickListener): Builder {
            this.shareClickListener = listener
            return this
        }

        fun addFilter(filter: OptionFilter): Builder {
            this.optionFilterList.add(filter)
            return this
        }

        fun onCancel(listener: OnCancelListener): Builder {
            this.cancelListener = listener
            return this
        }

        private fun createOptionList(): List<Option> {
            if (optionFilterList.isEmpty()) {
                return optionList
            }
            val newList = ArrayList<Option>()
            optionList.forEach { option ->
                if (filterOption(option)) {
                    newList.add(option)
                }
            }
            return newList
        }

        private fun filterOption(option: Option): Boolean {
            optionFilterList.forEach {
                if (!it.isShowOption(option)) {
                    return false
                }
            }
            return true
        }

        fun show() {
            OptionDialog(build()).show()
        }

        private fun build(): Config {
            return Config(
                context,
                showShare,
                cancelable,
                createOptionList().toImmutableList(),
                optionClickListener ?: OnOptionClickListener { _, _ -> },
                shareClickListener ?: OnShareClickListener { _, _ -> },
                cancelListener ?: OnCancelListener {}
            )
        }

    }

    fun interface OnOptionClickListener {
        fun onOptionClick(dialog: OptionDialog, option: Option)
    }

    fun interface OnShareClickListener {
        fun onShareClick(dialog: OptionDialog, share: ShareType)
    }

    fun interface OptionFilter {
        fun isShowOption(option: Option): Boolean
    }

    fun interface OnCancelListener {
        fun onCancel(dialog: OptionDialog)
    }

}