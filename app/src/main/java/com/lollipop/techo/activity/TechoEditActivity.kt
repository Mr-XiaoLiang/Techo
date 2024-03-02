package com.lollipop.techo.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lollipop.base.list.ItemTouchState
import com.lollipop.base.list.attachTouchHelper
import com.lollipop.base.listener.BackPressHandler
import com.lollipop.base.util.ActivityLauncherHelper
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.isCreated
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.onClick
import com.lollipop.base.util.onUI
import com.lollipop.base.util.registerResult
import com.lollipop.brackets.core.Stateless
import com.lollipop.brackets.core.TypedResponse
import com.lollipop.pigment.Pigment
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoItem
import com.lollipop.techo.data.TechoItemType
import com.lollipop.techo.data.TechoItemType.CheckBox
import com.lollipop.techo.data.TechoItemType.Number
import com.lollipop.techo.data.TechoItemType.Photo
import com.lollipop.techo.data.TechoItemType.Recording
import com.lollipop.techo.data.TechoItemType.Split
import com.lollipop.techo.data.TechoItemType.Text
import com.lollipop.techo.data.TechoItemType.Vcr
import com.lollipop.techo.data.TechoMode
import com.lollipop.techo.data.TechoTheme
import com.lollipop.techo.databinding.ActivityTechoEditFloatingBinding
import com.lollipop.techo.dialog.OptionMenuDialog
import com.lollipop.techo.dialog.options.Item
import com.lollipop.techo.edit.EditManager
import com.lollipop.techo.fragment.RichTextOptionFragment
import com.lollipop.techo.list.detail.DetailListAdapter
import com.lollipop.techo.list.detail.EditHolder
import com.lollipop.techo.util.CircleAnimationGroup
import org.json.JSONObject

/**
 * 编辑 & 添加页
 */
class TechoEditActivity : BasicListActivity(),
    TechoMode.EditStateListener,
    EditHolder.OnItemOptionButtonClickListener {

    companion object {

        private const val PARAMETER_TECHO_ID = "PARAMETER_TECHO_ID"

        private const val RESULT_TECHO_ID = "RESULT_TECHO_ID"

        const val NO_ID = 0

        fun start(context: Context, infoId: Int = 0, intentInit: (Intent) -> Unit = {}) {
            val intent = Intent(context, TechoEditActivity::class.java)
            putParams(intent, infoId)
            intentInit(intent)
            context.startActivity(intent)
        }

        fun putParams(intent: Intent, infoId: Int = 0): Intent {
            return intent.putExtra(PARAMETER_TECHO_ID, infoId)
        }

        fun getResultTechoId(intent: Intent?): Int {
            intent ?: return NO_ID
            return intent.getIntExtra(RESULT_TECHO_ID, NO_ID)
        }

        fun putResultTechoId(intent: Intent, id: Int) {
            intent.putExtra(RESULT_TECHO_ID, id)
        }

        val LAUNCHER: Class<out ActivityResultContract<Int?, Int>> = ResultContract::class.java

    }

    private val mode by lazy {
        TechoMode.edit(this).attach(this).bind(this.lifecycle).build()
    }

    override val optionsMenu = true

    private val floatingBinding: ActivityTechoEditFloatingBinding by lazyBind()

    override val useCustomPigment: Boolean = true

    override val floatingView: View
        get() = floatingBinding.root

    private val circleAnimationGroup by lazy {
        CircleAnimationGroup()
    }

    private val floatingButtonManger = FloatingButtonManger()

    private val techoId by lazy {
        intent.getIntExtra(PARAMETER_TECHO_ID, NO_ID)
    }

    private var quickAddType = Text

    private val editManager by lazy {
        EditManager(this, floatingBinding.editContainer)
    }

    private val richTextOptionLauncher = registerResult(
        RichTextOptionFragment.LAUNCHER,
        ::onRichTextOptionResult
    )

    private val backPressHandler = BackPressHandler {
        closeCircle()
    }

    private val contentAdapter by lazy {
        DetailListAdapter(mode.itemList, this).apply {
            changeEditMode(true)
        }
    }

    private var customPigment: TechoTheme? = null

    private val themeSelectLauncher = registerResult(
        TechoThemeSelectActivity.LAUNCHER
    ) {
        val theme = TechoTheme.findOrNull(it)
        if (theme != null) {
            useCustomPigment(theme)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        floatingBinding.floatingButtonPanel.fixInsetsByPadding(WindowInsetsEdge.ALL)
        editManager.initPanel()
        initContentView()
        initMenuBtn()
        initData()
    }

    private fun initContentView() {
        initRecyclerView { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(
                this, RecyclerView.VERTICAL, false
            )
            recyclerView.adapter = contentAdapter
            recyclerView.attachTouchHelper()
                .canDrag(true)
                .canSwipe(true)
                .onMove(mode)
                .onSwipe(mode)
                .onStatusChange(::onItemTouchStateChanged)
                .apply()
        }
    }

    private fun initData() {
        onUI {
            mode.loadOrCreate(techoId)
        }
    }

    private fun initMenuBtn() {
        circleAnimationGroup.setCenterView(floatingBinding.floatingMenuBtn)
        floatingButtonManger.bind(
            floatingBinding.floatingTextBtn to Text,
            floatingBinding.floatingNumberBtn to Number,
            floatingBinding.floatingCheckboxBtn to CheckBox,
            floatingBinding.floatingPhotoBtn to Photo,
            floatingBinding.floatingSplitBtn to Split,
            floatingBinding.floatingRecorderBtn to Recording,
            floatingBinding.floatingVcrBtn to Vcr,
            floatingBinding.floatingTest8 to null,
            floatingBinding.floatingTest9 to null,
            floatingBinding.floatingTest10 to null,
            floatingBinding.floatingTest11 to null,
            floatingBinding.floatingTest12 to null,
            floatingBinding.floatingTest13 to null,
            floatingBinding.floatingTest14 to null,
        )
        floatingButtonManger.setup(circleAnimationGroup)
        floatingButtonManger.onClick(::onFloatBtnClick)
        circleAnimationGroup.bindListener {
            onProgressUpdate { progress ->
                floatingBinding.floatingMenuBtn.rotation = progress * 135
            }
            onHideCalled {
                floatingBinding.quickAddButton.show()
            }
            onShowCalled {
                floatingBinding.quickAddButton.hide()
            }
        }
        floatingBinding.quickAddButton.onClick {
            addItemByType()
        }
        floatingBinding.floatingMenuBtn.onClick {
            if (!circleAnimationGroup.isOpened) {
                openCircle()
            } else {
                closeCircle()
            }
        }

        // 初始化默认的类型
        quickAddType = Text
        floatingBinding.quickAddButton.setImageDrawable(floatingBinding.floatingTextBtn.drawable)
        circleAnimationGroup.hide()
    }

    private fun closeCircle() {
        circleAnimationGroup.close()
        backPressHandler.isEnabled = false
    }

    private fun openCircle() {
        circleAnimationGroup.open()
        backPressHandler.isEnabled = true
    }

    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        if (customPigment == null) {
            onCustomDecorationChanged(TechoTheme.valueOf(pigment))
        }
    }

    private fun useCustomPigment(pigment: TechoTheme) {
        customPigment = pigment
        onCustomDecorationChanged(pigment.getPigment())
    }

    private fun onCustomDecorationChanged(snapshot: TechoTheme.Snapshot) {
        headerDarkMode(snapshot.isDarkMode)
        val fabCount = floatingBinding.floatingButtonPanel.childCount
        for (index in 0 until fabCount) {
            floatingBinding.floatingButtonPanel.getChildAt(index)?.let {
                when (it) {
                    is ExtendedFloatingActionButton -> {
                        snapshot.tint(it)
                    }

                    is FloatingActionButton -> {
                        snapshot.tint(it)
                    }
                }
            }
        }
        scaffoldBinding.contentRoot.setBackgroundColor(snapshot.backgroundColor)
        contentAdapter.onDecorationChanged(snapshot)
    }

    override fun OptionMenuDialog.OptionScope.onCreateOptionsMenu(dialog: OptionMenuDialog) {
        Item {
            title = Stateless(getString(R.string.theme))
            onClick = TypedResponse {
                themeSelectLauncher.launch(TechoThemeSelectActivity.LaunchInput(true))
            }
        }
        Item {
            title = Stateless(getString(R.string.done))
            onClick = TypedResponse {
                mode.update {
                    if (isCreated()) {
                        resultOk { putResultTechoId(it, mode.infoId) }
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        }
    }

    private fun onFloatBtnClick(btn: FloatingActionButton, type: TechoItemType) {
        floatingBinding.quickAddButton.setImageDrawable(btn.drawable)
        quickAddType = type
        closeCircle()
        addItemByType()
    }

    private fun onItemTouchStateChanged(
        viewHolder: RecyclerView.ViewHolder?,
        status: ItemTouchState
    ) {
        if (status == ItemTouchState.IDLE) {
            mode.format()
        }
    }

    private fun addItemByType() {
        mode.insert(quickAddType)
    }

    override fun onLoadStart() {
        showLoading()
    }

    override fun onLoadEnd() {
        hideLoading()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onInfoChanged(first: Int, second: Int, type: TechoMode.ChangedType) {
        mode.onInfoChangedDefaultImpl(recyclerView.adapter, first, second, type)
    }

    override fun onDestroy() {
        super.onDestroy()
        circleAnimationGroup.destroy()
        editManager.destroy()
    }

    override fun onItemOptionButtonClick(holder: EditHolder<*>) {
        val adapterPosition = holder.adapterPosition
        when (val item = mode.itemList[adapterPosition]) {
            is TechoItem.Text,
            is TechoItem.CheckBox,
            is TechoItem.Number -> {
                richTextOptionLauncher.launch(RichTextOptionFragment.Request(adapterPosition, item))
            }

            is TechoItem.Photo -> {
                // TODO()
            }

            is TechoItem.Split -> {
                // TODO()
            }

            is TechoItem.Title -> {
                // TODO()
            }

            is TechoItem.Recording -> {
                // TODO()
            }

            is TechoItem.Vcr -> {
                // TODO()
            }
        }
    }

    private fun onRichTextOptionResult(result: RichTextOptionFragment.Result) {
        if (!result.success) {
            return
        }
        if (result.input.isEmpty()) {
            return
        }
        try {
            val position = result.key
            if (position in mode.itemList.indices) {
                val item = mode.itemList[position]
//                val input = JSONObject(result.input)
//                val srcInfo = TechoItem.createItem(item.itemType)
//                srcInfo.parse(input)
//                if (srcInfo.value == item.value) {
//                    item.parse(JSONObject(result.info))
//                    viewBinding.contentListView.adapter?.notifyItemChanged(position)
//                }
                item.parse(JSONObject(result.info))
                recyclerView.adapter?.notifyItemChanged(position)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onItemEditButtonClick(holder: EditHolder<*>) {
        val adapterPosition = holder.adapterPosition
        val item = mode.itemList[adapterPosition]
        editManager.openEditPanel(adapterPosition, item) { index, _ ->
            recyclerView.adapter?.notifyItemChanged(index)
        }
    }

    private class FloatingButtonManger {
        private val holderList = ArrayList<FloatingButtonHolder>()

        private var clickCallback: ((FloatingActionButton, TechoItemType) -> Unit) = { _, _ -> }

        fun bind(vararg views: Pair<FloatingActionButton, TechoItemType?>) {
            views.forEach {
                holderList.add(FloatingButtonHolder(it.first, it.second, ::onHolderClick))
            }
        }

        fun setup(group: CircleAnimationGroup) {
            group.reset(holderList)
        }

        private fun onHolderClick(holder: FloatingButtonHolder) {
            if (holder.isEnable && holder.type != null) {
                this.clickCallback(holder.button, holder.type)
            }
        }

        fun onClick(callback: (FloatingActionButton, TechoItemType) -> Unit) {
            this.clickCallback = callback
        }

    }

    private class FloatingButtonHolder(
        val button: FloatingActionButton,
        val type: TechoItemType?,
        private val onClickCallback: (FloatingButtonHolder) -> Unit
    ) : CircleAnimationGroup.Holder(button), View.OnClickListener {

        init {
            view.setOnClickListener(this)
        }

        override val isEnable: Boolean
            get() {
                return type != null
            }

        override fun onClick(v: View?) {
            type?.let {
                onClickCallback(this)
            }
        }

    }

    class ResultContract : ActivityLauncherHelper.Simple<Int?, Int>() {

        override val activityClass: Class<out Activity> = TechoEditActivity::class.java

        override fun putParams(intent: Intent, input: Int?) {
            super.putParams(intent, input)
            Companion.putParams(intent, input ?: NO_ID)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Int {
            intent ?: return NO_ID
            if (resultCode != Activity.RESULT_OK) {
                return NO_ID
            }
            return getResultTechoId(intent)
        }

    }

}