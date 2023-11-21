package com.lollipop.lqrdemo.creator.subpage

import androidx.recyclerview.widget.RecyclerView
import com.lollipop.lqrdemo.creator.subpage.adjust.StyleAdjustFragment
import com.lollipop.lqrdemo.databinding.ItemQrPositionDetectionTabBinding

/**
 * 主要定位点的设置
 */
class QrPositionDetectionFragment : StyleAdjustFragment() {

    private class TabHolder(
        val binding: ItemQrPositionDetectionTabBinding
    ) : RecyclerView.ViewHolder(binding.root) {


    }

}