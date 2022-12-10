package com.lollipop.techo.qr

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.base.util.lazyBind
import com.lollipop.qr.comm.BarcodeInfo
import com.lollipop.techo.activity.HeaderActivity
import com.lollipop.techo.databinding.ActivityQrResultBinding

class QrResultActivity : HeaderActivity() {

    private val binding: ActivityQrResultBinding by lazyBind()

    override val contentView: View
        get() {
            return binding.root
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private class ItemAdapter(
        private val data: List<BarcodeInfo>
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }

        override fun getItemViewType(position: Int): Int {
            when (data[position]) {
                is BarcodeInfo.Wifi -> TODO()
                is BarcodeInfo.CalendarEvent -> TODO()
                is BarcodeInfo.Contact -> TODO()
                is BarcodeInfo.DriverLicense -> TODO()
                is BarcodeInfo.Email -> TODO()
                is BarcodeInfo.GeoPoint -> TODO()
                is BarcodeInfo.Phone -> TODO()
                is BarcodeInfo.Sms -> TODO()
                is BarcodeInfo.Url -> TODO()
                is BarcodeInfo.Isbn -> TODO()
                is BarcodeInfo.Product -> TODO()
                is BarcodeInfo.Text -> TODO()
                is BarcodeInfo.Unknown -> TODO()
            }
            return super.getItemViewType(position)
        }

    }

}