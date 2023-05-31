package com.lollipop.lqrdemo.creator.content

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.dp2px
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.base.BaseFragment
import com.lollipop.lqrdemo.databinding.ItemContentBuilderInputBinding
import com.lollipop.pigment.BlendMode
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentWallpaperCenter

abstract class ContentBuilder : BaseFragment() {

    abstract fun getContentValue(): String

    private var contentGroup: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val group = RecyclerView(inflater.context)
        group.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        contentGroup = group
        return group
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val builder = Builder(view.context)
        buildContent(builder)
        val itemList = builder.build()
        super.onViewCreated(view, savedInstanceState)
        contentGroup?.layoutManager =
            LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)
        contentGroup?.adapter = ItemAdapter(itemList)
    }

    abstract fun buildContent(space: ItemSpace)

    protected fun ItemSpace.Space(height: Int) {
        add(SpaceItem(height))
    }

    protected fun ItemSpace.Input(
        label: String,
        config: InputConfig,
        presetValue: () -> CharSequence,
        onInputChanged: (CharSequence) -> Unit,
    ) {
        add(InputItem(label, config, presetValue, onInputChanged))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        contentGroup?.adapter?.notifyDataSetChanged()
    }

    private class ItemAdapter(val list: List<Item>) : RecyclerView.Adapter<ItemHolder>() {

        private var layoutInflater: LayoutInflater? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            val inflater = layoutInflater ?: LayoutInflater.from(parent.context)
            layoutInflater = inflater
            return ItemHolder(inflater.inflate(viewType, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemViewType(position: Int): Int {
            return list[position].viewId
        }

    }

    private class ItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: Item) {
            item.bind(itemView)
        }
    }

    private class Builder(override val context: Context) : ItemSpace {

        private val itemList = ArrayList<Item>()

        override fun add(item: Item) {
            itemList.add(item)
        }

        fun build(): List<Item> {
            val maxPosition = itemList.size - 1
            for (i in 0..maxPosition) {
                val item = itemList[i]
                val last: Item? = if (i > 0) {
                    itemList[i - 1]
                } else {
                    null
                }
                val next: Item? = if (i < maxPosition) {
                    itemList[i + 1]
                } else {
                    null
                }
                item.updateChain(last, next)
            }
            return itemList
        }

    }

    interface ItemSpace {

        val context: Context

        fun add(item: Item)

    }

    abstract class Item {

        abstract val viewId: Int

        abstract fun updateChain(last: Item?, next: Item?)

        abstract fun bind(view: View)

    }

    protected class SpaceItem(
        private val height: Int,
    ) : Item() {
        override val viewId: Int = R.layout.item_content_builder_space

        override fun updateChain(last: Item?, next: Item?) {
        }

        override fun bind(view: View) {
            val layoutParams = view.layoutParams ?: ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.height = height
            view.layoutParams = layoutParams
        }

    }

    protected class InputConfig(
        val inputType: Int,
        val maxLines: Int = Int.MAX_VALUE,
        val lines: Int = 1,
    )

    protected class InputItem(
        private val label: String,
        private val config: InputConfig,
        private val presetValue: () -> CharSequence,
        private val onInputChanged: (CharSequence) -> Unit,
    ) : Item() {

        private var mergeLast = false
        private var mergeNext = false

        override val viewId: Int = R.layout.item_content_builder_input

        override fun updateChain(last: Item?, next: Item?) {
            mergeLast = last is InputItem
            mergeNext = next is InputItem
        }

        override fun bind(view: View) {
            val binding = ItemContentBuilderInputBinding.bind(view)
            updateBounds(binding)
            binding.labelView.text = label
            binding.textInputView.onTextChanged(null)
            binding.textInputView.setText(presetValue())
            binding.textInputView.onTextChanged(onInputChanged)
            binding.textInputView.setLines(config.lines)
            binding.textInputView.inputType = config.inputType
            binding.textInputView.maxLines = config.maxLines
        }

        private fun updateBounds(binding: ItemContentBuilderInputBinding) {
            val radius = 12F.dp2px
            val topRadius = if (mergeLast) {
                0F
            } else {
                radius
            }
            val bottomRadius = if (mergeNext) {
                0F
            } else {
                radius
            }
            binding.itemClipLayout.setRadius(
                leftTop = topRadius,
                rightTop = topRadius,
                leftBottom = bottomRadius,
                rightBottom = bottomRadius
            )
            binding.dividerLine.isVisible = mergeNext
            PigmentWallpaperCenter.pigment?.let {
                binding.itemContentView.setBackgroundColor(
                    when (it.blendMode) {
                        BlendMode.Dark -> {
                            Color.BLACK
                        }

                        BlendMode.Light -> {
                            Color.WHITE
                        }
                    }
                )
                binding.dividerLine.setBackgroundColor(it.onBackgroundBody)
                binding.textInputView.setTextColor(it.onBackgroundTitle)
                binding.textInputView.setTextColor(it.onBackgroundBody)
                binding.labelView.setTextColor(it.onBackgroundBody)
            }
        }

    }

}