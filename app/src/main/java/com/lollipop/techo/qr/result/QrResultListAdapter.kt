package com.lollipop.techo.qr.result

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lollipop.qr.comm.BarcodeInfo

class QrResultListAdapter(
    private val data: List<BarcodeInfo>
): RecyclerView.Adapter<QrResultRootHolder<*>>() {

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_WIFI = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrResultRootHolder<*> {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: QrResultRootHolder<*>, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is BarcodeInfo.CalendarEvent -> TODO()
            is BarcodeInfo.Contact -> TODO()
            is BarcodeInfo.DriverLicense -> TODO()
            is BarcodeInfo.Email -> TODO()
            is BarcodeInfo.GeoPoint -> TODO()
            is BarcodeInfo.Phone -> TODO()
            is BarcodeInfo.Sms -> TODO()
            is BarcodeInfo.Url -> TODO()
            is BarcodeInfo.Isbn,
            is BarcodeInfo.Product,
            is BarcodeInfo.Text,
            is BarcodeInfo.Unknown -> {
                TYPE_TEXT
            }
            is BarcodeInfo.Wifi -> {
                TYPE_WIFI
            }
        }
    }

}