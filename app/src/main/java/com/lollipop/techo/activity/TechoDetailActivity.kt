package com.lollipop.techo.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.lollipop.base.util.isCreated
import com.lollipop.base.util.lazyBind
import com.lollipop.base.util.registerResult
import com.lollipop.techo.R
import com.lollipop.techo.data.TechoMode
import com.lollipop.techo.databinding.ActivityTechoDetailBinding

/**
 * 详情页
 */
class TechoDetailActivity : HeaderActivity(), TechoMode.StateListener {

    companion object {

        private const val PARAMETER_TECHO_ID = "PARAMETER_TECHO_ID"

        private const val RESULT_TECHO_ID = "RESULT_TECHO_ID"

        const val NO_ID = 0

        fun putParams(intent: Intent, infoId: Int = 0): Intent {
            return intent.putExtra(PARAMETER_TECHO_ID, infoId)
        }

        fun getResultTechoId(intent: Intent?): Int {
            intent ?: return NO_ID
            return intent.getIntExtra(RESULT_TECHO_ID, NO_ID)
        }

        private fun setResultTechoId(intent: Intent, id: Int) {
            intent.putExtra(RESULT_TECHO_ID, id)
        }

    }

    private val editPageLauncher = registerResult(TechoEditActivity.LAUNCHER) { id ->
        if (isCreated()) {
            mode.load(id)
            resultOk { setResultTechoId(it, id) }
        }
    }

    override val optionsMenu: Int
        get() = R.menu.menu_detail

    private val viewBinding: ActivityTechoDetailBinding by lazyBind()

    private val mode by lazy {
        TechoMode.create(this).bind(this.lifecycle).attach(this).buildDetailMode()
    }

    private val techoId: Int
        get() {
            return intent.getIntExtra(PARAMETER_TECHO_ID, 0)
        }

    override val contentView: View
        get() = viewBinding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode.load(techoId)
    }

    override fun onLoadStart() {
        showLoading()
    }

    override fun onLoadEnd() {
        hideLoading()
    }

    override fun onInfoChanged(first: Int, second: Int, type: TechoMode.ChangedType) {
        TechoMode.onInfoChangedDefaultImpl(viewBinding.recyclerView.adapter, first, second, type)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuEdit -> {
                editPageLauncher.launch(techoId)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}