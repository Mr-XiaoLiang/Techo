package com.lollipop.techo.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.ActivityLauncherHelper
import com.lollipop.base.util.onUI
import com.lollipop.base.util.registerResult
import com.lollipop.brackets.core.Stateless
import com.lollipop.brackets.core.TypedResponse
import com.lollipop.pigment.Pigment
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoInfo
import com.lollipop.techo.data.TechoMode
import com.lollipop.techo.data.TechoTheme
import com.lollipop.techo.dialog.OptionMenuDialog
import com.lollipop.techo.dialog.options.Item
import com.lollipop.techo.list.detail.DetailListAdapter
import com.lollipop.techo.list.detail.EditHolder

/**
 * 编辑 & 添加页
 */
class TechoDetailActivity : BasicListActivity(),
    TechoMode.DetailStateListener,
    EditHolder.OnItemOptionButtonClickListener {

    companion object {

        private const val PARAMETER_TECHO_ID = "PARAMETER_TECHO_ID"

        private const val RESULT_TECHO_ID = "RESULT_TECHO_ID"

        const val NO_ID = 0

        fun start(context: Context, infoId: Int = 0, intentInit: (Intent) -> Unit = {}) {
            val intent = Intent(context, TechoDetailActivity::class.java)
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
        TechoMode.detail(this).attach(this).bind(this.lifecycle).build()
    }

    override val optionsMenu = true

    override val useCustomPigment: Boolean = true

    private val techoId by lazy {
        intent.getIntExtra(PARAMETER_TECHO_ID, NO_ID)
    }

    private val contentAdapter by lazy {
        DetailListAdapter(mode.info.items, this).apply {
            changeEditMode(false)
        }
    }

    private var customPigment: TechoTheme? = null

    private val editLauncher = registerResult(TechoEditActivity.LAUNCHER) {
        // 进入这个页面，都默认已经有了id了，所以直接刷新
        initData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initContentView()
        initData()
    }

    private fun initContentView() {
        initRecyclerView { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(
                this, RecyclerView.VERTICAL, false
            )
            recyclerView.adapter = contentAdapter
        }
    }

    private fun initData() {
        onUI {
            mode.load(techoId)
        }
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
        scaffoldBinding.contentRoot.setBackgroundColor(snapshot.backgroundColor)
        contentAdapter.onDecorationChanged(snapshot)
    }

    override fun OptionMenuDialog.OptionScope.onCreateOptionsMenu(dialog: OptionMenuDialog) {
        Item {
            title = Stateless(getString(R.string.edit))
            onClick = TypedResponse {
                editLauncher.launch(techoId)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onInfoChanged(info: TechoInfo) {
        recyclerView.adapter?.notifyDataSetChanged()
        // TODO("更新主题信息")
    }

    override fun onLoadStart() {
        showLoading()
    }

    override fun onLoadEnd() {
        hideLoading()
    }

    override fun onItemOptionButtonClick(holder: EditHolder<*>) {
    }

    override fun onItemEditButtonClick(holder: EditHolder<*>) {
    }

    class ResultContract : ActivityLauncherHelper.Simple<Int?, Int>() {

        override val activityClass: Class<out Activity> = TechoDetailActivity::class.java

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