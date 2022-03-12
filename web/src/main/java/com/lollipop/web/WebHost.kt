package com.lollipop.web

import android.app.Activity
import androidx.lifecycle.LifecycleOwner

interface WebHost {

    val hostActivity: Activity?

    val hostLifecycleOwner: LifecycleOwner?

}