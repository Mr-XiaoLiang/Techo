package com.lollipop.lqrdemo.router

import com.lollipop.qr.comm.BarcodeInfo

object WifiRouter : BarcodeRouter<BarcodeInfo.Wifi>() {

    // 需要权限，不搞了
//    override fun open(context: Context, barcodeInfo: BarcodeInfo.Wifi): Boolean {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            return false
//        }
//        val suggestionsList =
//            if (barcodeInfo.encryptionType == BarcodeInfo.Wifi.EncryptionType.OPEN) {
//                listOf(
//                    WifiNetworkSuggestion.Builder()
//                        .setSsid(barcodeInfo.ssid)
//                        .setIsAppInteractionRequired(false) // Optional (Needs location permission)
//                        .build()
//                )
//            } else {
//                listOf(
//                    WifiNetworkSuggestion.Builder()
//                        .setSsid(barcodeInfo.ssid)
//                        .setWpa2Passphrase(barcodeInfo.password)
//                        .setIsAppInteractionRequired(false) // Optional (Needs location permission)
//                        .build(),
//                    WifiNetworkSuggestion.Builder()
//                        .setSsid(barcodeInfo.ssid)
//                        .setWpa3Passphrase(barcodeInfo.password)
//                        .setIsAppInteractionRequired(false) // Optional (Needs location permission)
//                        .build()
//                )
//            }
//
//        val wifiManager =
//            context.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return false
//        wifiManager.addNetworkSuggestions(suggestionsList)
//        return super.open(context, barcodeInfo)
//    }

}