package com.lollipop.lqrdemo.creator.content

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
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
import org.json.JSONObject
import kotlin.reflect.KProperty

abstract class ContentBuilder : BaseFragment() {

    companion object {
        private const val KEY_VALUE_MAP_STRING = "KEY_VALUE_MAP_STRING"
        private const val KEY_VALUE_MAP_INT = "KEY_VALUE_MAP_INT"
    }

    private val stringValuesMap = HashMap<String, String>()
    private val intValuesMap = HashMap<String, Int>()

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val stringJson = JSONObject()
        stringValuesMap.forEach { entry ->
            stringJson.put(entry.key, entry.value)
        }
        outState.putString(KEY_VALUE_MAP_STRING, stringJson.toString())
        val intJson = JSONObject()
        intValuesMap.forEach { entry ->
            intJson.put(entry.key, entry.value)
        }
        outState.putString(KEY_VALUE_MAP_INT, intJson.toString())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState ?: return
        val stringJsonValue = savedInstanceState.getString(KEY_VALUE_MAP_STRING, "") ?: ""
        if (stringJsonValue.isNotEmpty()) {
            try {
                val json = JSONObject(stringJsonValue)
                json.keys().forEach {
                    stringValuesMap[it] = json.optString(it) ?: ""
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        val intJsonValue = savedInstanceState.getString(KEY_VALUE_MAP_INT, "") ?: ""
        if (intJsonValue.isNotEmpty()) {
            try {
                val json = JSONObject(intJsonValue)
                json.keys().forEach {
                    intValuesMap[it] = json.optInt(it, 0)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        notifyStateChanged()
    }

    abstract fun buildContent(space: ItemSpace)

    @SuppressLint("NotifyDataSetChanged")
    protected fun notifyStateChanged() {
        contentGroup?.adapter?.notifyDataSetChanged()
    }

    protected fun ItemSpace.SpaceEnd(height: Int = 26.dp2px) {
        add(SpaceItem(height))
    }

    protected fun ItemSpace.Space(height: Int = 16.dp2px) {
        add(SpaceItem(height))
    }

    protected fun ItemSpace.Input(
        @StringRes label: Int,
        config: InputConfig,
        presetValue: () -> String,
        onInputChanged: (String) -> Unit,
    ) {
        Input(context.getString(label), config, presetValue, onInputChanged)
    }

    protected fun ItemSpace.Input(
        label: String,
        config: InputConfig,
        presetValue: () -> String,
        onInputChanged: (String) -> Unit,
    ) {
        add(InputItem(label, config, presetValue, onInputChanged))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        contentGroup?.adapter?.notifyDataSetChanged()
    }

    protected inline fun <reified B : ContentBuilder> B.remember(
        noinline defValue: () -> String = { "" },
    ) = StringValueDelegate<B>(defValue)

    protected inline fun <reified B : ContentBuilder> B.rememberInt(
        noinline defValue: () -> Int = { 0 },
    ) = IntValueDelegate<B>(defValue)

    protected fun setValue(key: String, value: String) {
        stringValuesMap[key] = value
    }

    protected fun getValue(key: String): String? {
        return stringValuesMap[key]
    }

    protected fun setInt(key: String, value: Int) {
        intValuesMap[key] = value
    }

    protected fun getInt(key: String): Int? {
        return intValuesMap[key]
    }

    protected class StringValueDelegate<B : ContentBuilder>(val defValue: () -> String) {
        operator fun getValue(b: B, property: KProperty<*>): String {
            val valueName = property.name
            return b.getValue(valueName) ?: defValue()
        }

        operator fun setValue(b: B, property: KProperty<*>, value: String) {
            b.setValue(property.name, value)
        }
    }

    protected class IntValueDelegate<B : ContentBuilder>(val defValue: () -> Int) {
        operator fun getValue(b: B, property: KProperty<*>): Int {
            val valueName = property.name
            return b.getInt(valueName) ?: defValue()
        }

        operator fun setValue(b: B, property: KProperty<*>, value: Int) {
            b.setInt(property.name, value)
        }
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
    ) {

        companion object {

            private fun flags(vararg flag: Int): Int {
                var value = 0
                flag.forEach {
                    value = value.or(it)
                }
                return value
            }

            val PHONE = InputConfig(
                inputType = flags(
                    InputType.TYPE_CLASS_NUMBER,
                    InputType.TYPE_NUMBER_VARIATION_NORMAL
                )
            )

            val URL = InputConfig(
                inputType = flags(
                    InputType.TYPE_CLASS_TEXT,
                    InputType.TYPE_TEXT_VARIATION_URI
                )
            )

            val SUBJECT = InputConfig(
                inputType = flags(
                    InputType.TYPE_CLASS_TEXT,
                    InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT
                ),
            )

            val NORMAL = InputConfig(
                inputType = flags(
                    InputType.TYPE_CLASS_TEXT,
                    InputType.TYPE_TEXT_VARIATION_NORMAL
                )
            )

            val CONTENT = InputConfig(
                inputType = flags(
                    InputType.TYPE_CLASS_TEXT,
                    InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE,
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE
                )
            )

            val NAME = InputConfig(
                inputType = flags(
                    InputType.TYPE_CLASS_TEXT,
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                ),
            )

            val EMAIL = InputConfig(
                inputType = flags(
                    InputType.TYPE_CLASS_TEXT,
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                ),
            )
        }

    }

    protected class InputItem(
        private val label: String,
        private val config: InputConfig,
        private val presetValue: () -> String,
        private val onInputChanged: (String) -> Unit,
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
            if (config.lines > 1) {
                binding.textInputView.gravity = Gravity.START.or(Gravity.TOP)
            } else {
                binding.textInputView.gravity = Gravity.START.or(Gravity.CENTER_VERTICAL)
            }
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
                binding.dividerLine.alpha = 0.2F
                binding.textInputView.setTextColor(it.onBackgroundTitle)
                binding.textInputView.setTextColor(it.onBackgroundBody)
                binding.labelView.setTextColor(it.onBackgroundBody)
            }
        }

    }

}