package com.lollipop.lqrdemo.other

import android.content.Intent
import android.service.quicksettings.TileService
import com.lollipop.lqrdemo.MainActivity

class ScanTileService: TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}