package com.lollipop.techo.activity

import android.content.Intent
import android.view.View
import com.lollipop.base.util.lazyBind
import com.lollipop.techo.databinding.ActivityTechoDetailBinding

/**
 * 详情页
 */
class TechoDetailActivity : HeaderActivity() {

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

    override val contentView: View
        get() = viewBinding.root

}