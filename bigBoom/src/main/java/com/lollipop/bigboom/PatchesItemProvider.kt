package com.lollipop.bigboom

import android.view.ViewGroup

interface PatchesItemProvider {

    fun createHolder(parent: ViewGroup): PatchesHolder

}