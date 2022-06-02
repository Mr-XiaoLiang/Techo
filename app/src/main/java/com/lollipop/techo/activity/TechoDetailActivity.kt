package com.lollipop.techo.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.lollipop.base.util.lazyBind
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

    }

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
        mode.loadOrCreate(techoId)
    }

    override fun onLoadStart() {
        showLoading()
    }

    override fun onLoadEnd() {
        hideLoading()
    }

    override fun onInfoChanged(first: Int, second: Int, type: TechoMode.ChangedType) {
        mode.onInfoChangedDefaultImpl(viewBinding.recyclerView.adapter, first, second, type)
    }


}