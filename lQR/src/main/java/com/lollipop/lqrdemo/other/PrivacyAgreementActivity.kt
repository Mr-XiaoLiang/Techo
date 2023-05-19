package com.lollipop.lqrdemo.other

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.bind
import com.lollipop.base.util.insets.WindowInsetsEdge
import com.lollipop.base.util.insets.WindowInsetsHelper
import com.lollipop.base.util.insets.fixInsetsByPadding
import com.lollipop.base.util.lazyBind
import com.lollipop.lqrdemo.R
import com.lollipop.lqrdemo.base.ColorModeActivity
import com.lollipop.lqrdemo.databinding.ActivityPrivacyAgreementBinding
import com.lollipop.lqrdemo.databinding.ItemPrivacyAgreementBinding
import com.lollipop.pigment.Pigment
import com.lollipop.pigment.PigmentWallpaperCenter
import com.lollipop.privacy.PrivacyAgreementAdapter
import com.lollipop.privacy.PrivacyAgreementHolder
import com.lollipop.privacy.PrivacyAgreementItem

class PrivacyAgreementActivity : ColorModeActivity() {

    private val binding: ActivityPrivacyAgreementBinding by lazyBind()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        WindowInsetsHelper.initWindowFlag(this)
        binding.root.fixInsetsByPadding(WindowInsetsEdge.ALL)
        bindByBack(binding.backButton)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = PrivacyAgreementAdapter(getPrivacyAgreement()) {
            Holder(it.bind(false))
        }
    }

    private fun getPrivacyAgreement(): List<PrivacyAgreementItem> {
        return listOf(
            PrivacyAgreementItem(
                R.string.privacy_agreement_label1,
                R.string.privacy_agreement_content1
            ),
            PrivacyAgreementItem(
                R.string.privacy_agreement_label2,
                R.string.privacy_agreement_content2
            ),
            PrivacyAgreementItem(
                R.string.privacy_agreement_label3,
                R.string.privacy_agreement_content3
            ),
            PrivacyAgreementItem(
                R.string.privacy_agreement_label4,
                R.string.privacy_agreement_content4
            ),
            PrivacyAgreementItem(
                R.string.privacy_agreement_label5,
                R.string.privacy_agreement_content5
            ),
            PrivacyAgreementItem(
                R.string.privacy_agreement_label6,
                R.string.privacy_agreement_content6
            ),
        )
    }


    private class Holder(
        private val binding: ItemPrivacyAgreementBinding,
    ) : PrivacyAgreementHolder(binding.root) {
        override fun getLabelView(): TextView {
            return binding.agreementLabelView
        }

        override fun getContentView(): TextView {
            return binding.agreementContentView
        }

        override fun onBind(item: PrivacyAgreementItem) {
            super.onBind(item)
            PigmentWallpaperCenter.pigment?.let { pigment ->
                binding.agreementLabelView.setTextColor(pigment.onBackgroundTitle)
                binding.agreementContentView.setTextColor(pigment.onBackgroundBody)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDecorationChanged(pigment: Pigment) {
        super.onDecorationChanged(pigment)
        binding.recyclerView.adapter?.notifyDataSetChanged()
        binding.root.setBackgroundColor(pigment.backgroundColor)
        binding.backButton.imageTintList = ColorStateList.valueOf(pigment.onBackgroundTitle)
        binding.titleView.setTextColor(pigment.onBackgroundTitle)
    }
}