package com.lollipop.qr.writer

import androidx.lifecycle.LifecycleOwner
import com.lollipop.qr.comm.BarcodeInfo
import kotlin.math.min

class TypedBarcodeWriter(lifecycleOwner: LifecycleOwner) : BarcodeWriter(lifecycleOwner) {

    companion object {
        private val SPECIAL = arrayOf(
            Part("\\", "\\\\"),
            Part(";", "\\;"),
            Part(",", "\\,"),
            Part(":", "\\:")
        )

        private fun String.encode(): String {
            var value = this
            if (value.isEmpty()) {
                return value
            }
            SPECIAL.forEach { special ->
                value = value.replace(special.key, special.value)
            }
            return value
        }

    }

    private class Part(
        val key: String,
        val value: String
    )

    fun encode(info: BarcodeInfo): Builder {
        return when (info) {
            is BarcodeInfo.CalendarEvent -> {
                // TODO() 还不知道怎么写，姑且随便打印吧
                encode(info.toString())
            }
            is BarcodeInfo.Contact -> {
                encode(info)
            }
            is BarcodeInfo.DriverLicense -> {
                // TODO() 还不知道怎么写，姑且随便打印吧
                encode(info.toString())
            }
            is BarcodeInfo.Email -> {
                encode(info)
            }
            is BarcodeInfo.GeoPoint -> {
                // TODO() 还不知道怎么写，姑且随便打印吧
                encode(info.toString())
            }
            is BarcodeInfo.Isbn -> {
                encode(info.value)
            }
            is BarcodeInfo.Phone -> {
                encode(info)
            }
            is BarcodeInfo.Product -> {
                encode(info.value)
            }
            is BarcodeInfo.Sms -> {
                encode(info)
            }
            is BarcodeInfo.Text -> {
                encode(info.value)
            }
            is BarcodeInfo.Unknown -> {
                encode(info.value)
            }
            is BarcodeInfo.Url -> {
                encode(info.url)
            }
            is BarcodeInfo.Wifi -> {
                encode(info)
            }
        }
    }

    /**
     *  BEGIN:VCARD
     *  VERSION:4.0
     *  FN:Simon Perreault
     *  N:Perreault;Simon;;;ing. jr,M.Sc.
     *  BDAY:--0203
     *  GENDER:M
     *  EMAIL;TYPE=work:simon.perreault@viagenie.ca
     *  END:VCARD
     *
     *  https://en.wikipedia.org/wiki/VCard
     */
    private fun encode(info: BarcodeInfo.Contact): Builder {
        val vCard = VCard(true, "${info.name.first}${info.name.last}")
        vCard.add(
            "N",
            info.name.prefix,
            info.name.last,
            info.name.middle,
            info.name.first,
            info.name.suffix
        )
        vCard.add("ORG", info.organization)
        vCard.add("TITLE", info.title)
        info.phones.forEach {
            vCard.add("TEL", arrayOf(it.type.name), it.number)
        }
        info.emails.forEach {
            vCard.add("EMAIL", arrayOf(it.type.name), it.address)
        }
        info.urls.forEach {
            vCard.add("URL", it)
        }
        info.addresses.forEach {
            vCard.add("ADR", arrayOf(it.type.name), *it.lines)
        }
        vCard.end()
        return encode(vCard.toString())
    }

    /**
     * tel:123456678
     */
    private fun encode(info: BarcodeInfo.Phone): Builder {
        return encode("tel:${info.number.encode()}")
    }

    /**
     * SMSTO:1111:AAAAAA
     */
    private fun encode(info: BarcodeInfo.Sms): Builder {
        val number = info.phoneNumber.encode()
        val message = info.message.encode()
        return encode("SMSTO:${number}:${message}")
    }

    /**
     * WIFI:T:WEP;S:AAAA;P:CCCCC;I:BBBB;H:true;
     */
    private fun encode(info: BarcodeInfo.Wifi): Builder {
        val type = info.encryptionType.proto
        val ssid = info.ssid.encode()
        val pwd = info.password.encode()
        val name = info.username.encode()
        return encode("WIFI:T:${type};S:${ssid};P:${pwd};I:${name};H:;")
    }

    /**
     * mailto:AAAA?subject=BBBB&body=CCCCC
     */
    private fun encode(info: BarcodeInfo.Email): Builder {
        val address = info.address.encode()
        val subject = info.subject.encode()
        val body = info.body.encode()
        val type = info.type.proto
        return encode("mailto:${address}?subject=${subject}&body=${body}&type=${type}")
    }

    private class VCard(val autoWrap: Boolean, val fileName: String) {

        companion object {
            private val SPECIAL = arrayOf("\\", ";", ",")
            private const val MAX_LINE_LENGTH = 75
        }

        val contentBuilder = StringBuilder("BEGIN:VCARD\nVERSION:4.0\n")

        private var isAddEnd = false

        init {
            add("FN", fileName)
        }

        fun add(key: String, vararg value: String) {
            add(key, emptyArray(), *value)
        }

        fun add(key: String, arg: Array<String>, vararg value: String) {
            var lineLength = 0
            contentBuilder.append(key)
            lineLength += key.length
            arg.forEach {
                contentBuilder.append(";")
                lineLength += 1
                lineLength = addValue(lineLength, it)
            }
            contentBuilder.append(":")
            lineLength += 1
            for (i in value.indices) {
                if (i > 0) {
                    contentBuilder.append(";")
                    lineLength += 1
                }
                lineLength = addValue(lineLength, value[i])
            }
            contentBuilder.append("\n")
        }

        private fun addValue(lineLength: Int, value: String): Int {
            val encodeValue = value.encode()
            if (!autoWrap) {
                contentBuilder.append(encodeValue)
                return lineLength + encodeValue.length
            }
            var valueLength = encodeValue.length
            var valueIndex = 0
            var index = lineLength
            while (index + valueLength > MAX_LINE_LENGTH) {
                val subLength = min(valueLength, MAX_LINE_LENGTH - index)
                val subString = encodeValue.substring(valueIndex, subLength)
                valueIndex += subLength
                valueLength -= subLength
                contentBuilder.append(subString)
                contentBuilder.append("\n ")
                index = 1
            }
            if (valueLength > 0) {
                contentBuilder.append(encodeValue.substring(valueIndex, valueIndex + valueLength))
                index += valueLength
            }
            return index
        }

        fun end() {
            if (isAddEnd) {
                return
            }
            contentBuilder.append("END:VCARD")
            isAddEnd = true
        }

        override fun toString(): String {
            end()
            return contentBuilder.toString()
        }

    }

}