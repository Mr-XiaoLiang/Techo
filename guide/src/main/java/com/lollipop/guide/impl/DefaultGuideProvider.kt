package com.lollipop.guide.impl

import android.content.Context
import android.view.ViewGroup
import com.lollipop.guide.GuideProvider
import com.lollipop.guide.GuideStep

/**
 * @author lollipop
 * @date 2021/6/1 23:10
 * 默认的引导提供者
 */
class DefaultGuideProvider: GuideProvider() {

    override fun support(step: GuideStep): Boolean {
        return true
    }

    override fun onGuideBoundsChanged(left: Int, top: Int, right: Int, bottom: Int) {
        TODO("Not yet implemented")
    }

    override fun onTargetChange(step: GuideStep) {
        TODO("Not yet implemented")
    }

    private class GuideView(context: Context): ViewGroup(context) {
        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            TODO("Not yet implemented")
        }
    }

}