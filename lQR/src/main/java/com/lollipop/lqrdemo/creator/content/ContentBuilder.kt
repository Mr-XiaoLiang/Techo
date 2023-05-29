package com.lollipop.lqrdemo.creator.content

import com.lollipop.lqrdemo.base.BaseFragment

abstract class ContentBuilder : BaseFragment() {

    open fun getContentValue(): String {
        return ""
    }

}