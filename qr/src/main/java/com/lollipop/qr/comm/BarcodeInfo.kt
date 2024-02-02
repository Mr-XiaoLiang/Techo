package com.lollipop.qr.comm

import android.graphics.Point
import android.graphics.Rect
import android.provider.ContactsContract
import android.util.Size
import androidx.annotation.CallSuper
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.lollipop.qr.BarcodeFormat
import com.lollipop.qr.writer.TypedBarcodeWriter.Companion.encode
import org.json.JSONArray
import org.json.JSONObject

sealed class BarcodeInfo {

    companion object {
        private const val INFO_NAME = "infoName"
        private const val INFO_RAW = "infoRaw"
        private const val INFO_BARCODE_FORMAT = "infoBarcodeFormat"
    }

    protected inline fun <reified T : Any> T.className(): String {
        return this::class.java.name
    }

    protected open val useBuildBarcodeValueOnly = false

    var rawValue: String = ""
        private set
    var format: BarcodeFormat = BarcodeFormat.UNKNOWN
        private set

    fun setRaw(value: String, f: BarcodeFormat) {
        rawValue = value
        format = f
    }

    fun toJson(): String {
        val jsonObject = this.toJson { json, info ->
            info.save(json)
            json.put(INFO_NAME, className())
        }
        try {
            return jsonObject.toString()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return ""
    }

    fun fromJson(json: String): BarcodeInfo {
        try {
            val jsonObject = JSONObject(json)
            if (jsonObject.has(INFO_NAME)) {
                val name = jsonObject.optString(INFO_NAME)
                val instance = Class.forName(name).getDeclaredConstructor().newInstance()
                if (instance is BarcodeInfo) {
                    instance.resume(jsonObject)
                    return instance
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return Text().apply {
            value = json
            setRaw(json, BarcodeFormat.QR_CODE)
        }
    }

    @CallSuper
    internal open fun save(json: JSONObject) {
        json.put(INFO_RAW, rawValue)
        json.put(INFO_BARCODE_FORMAT, format.name)
    }

    @CallSuper
    internal open fun resume(json: JSONObject) {
        rawValue = json.optString(INFO_RAW)
        val formatName = json.optString(INFO_BARCODE_FORMAT)
        format = BarcodeFormat.entries.find { it.name == formatName } ?: BarcodeFormat.UNKNOWN
    }

    fun getBarcodeValue(): String {
        if (useBuildBarcodeValueOnly) {
            return buildBarcodeValue()
        }
        if (rawValue.isNotEmpty()) {
            return rawValue
        }
        return buildBarcodeValue()
    }

    protected abstract fun buildBarcodeValue(): String

    protected inline fun <reified T : Any> Collection<T>.mapToJson(map: (T) -> Any): JSONArray {
        val jsonArray = JSONArray()
        val list = this
        list.forEach {
            try {
                jsonArray.put(map(it))
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return jsonArray
    }

    protected inline fun <reified T : Any> T.toJson(map: (JSONObject, T) -> Unit): JSONObject {
        val jsonObject = JSONObject()
        try {
            map(jsonObject, this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return jsonObject
    }

    protected inline fun <reified T : Any> MutableList<T>.fromJson(
        json: JSONObject,
        name: String,
        map: (Any) -> T,
    ) {
        val list = this
        val jsonArray = json.optJSONArray(name) ?: return
        val length = jsonArray.length()
        for (i in 0 until length) {
            list.add(map(jsonArray.opt(i) ?: ""))
        }
    }

    protected inline fun <reified T : Any> MutableList<T>.fromJsonByObject(
        json: JSONObject,
        name: String,
        map: (JSONObject) -> T,
    ) {
        fromJson(json, name) { any ->
            map(
                if (any is JSONObject) {
                    any
                } else {
                    JSONObject()
                }
            )

        }
    }

    class Text : BarcodeInfo() {

        companion object {
            private const val VALUE = "value"
        }

        var value: String = ""

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            value = json.optString(VALUE)
        }

        override fun buildBarcodeValue(): String {
            return value
        }
    }

    class Unknown : BarcodeInfo() {

        companion object {
            private const val VALUE = "value"
        }

        var value: String = ""

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            value = json.optString(VALUE)
        }

        override fun buildBarcodeValue(): String {
            return value
        }
    }

    class Isbn : BarcodeInfo() {
        companion object {
            private const val VALUE = "value"
        }

        var value: String = ""

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            value = json.optString(VALUE)
        }

        override fun buildBarcodeValue(): String {
            return value
        }
    }

    class Product : BarcodeInfo() {
        companion object {
            private const val VALUE = "value"
        }

        var value: String = ""

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            value = json.optString(VALUE)
        }

        override fun buildBarcodeValue(): String {
            return value
        }
    }

    /**
     * 联系方式
     */
    class Contact : BarcodeInfo() {

        companion object {
            private const val PERSON = "person"
            private const val ORGANIZATION = "organization"
            private const val TITLE = "title"
            private const val ADDRESSES = "addresses"
            private const val EMAILS = "emails"
            private const val PHONES = "phones"
            private const val URLS = "urls"
        }

        var name: PersonName = PersonName()
        var organization: String = ""
        var title: String = ""
        val addresses: ArrayList<Address> = ArrayList()
        val emails: ArrayList<Email> = ArrayList()
        val phones: ArrayList<Phone> = ArrayList()
        val urls: ArrayList<String> = ArrayList()

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(PERSON, name.toJson { j, p -> p.save(j) })
            json.put(ORGANIZATION, organization)
            json.put(TITLE, title)
            json.put(ADDRESSES, addresses.mapToJson { it.toJson { j, a -> a.save(j) } })
            json.put(EMAILS, emails.mapToJson { it.toJson { j, e -> e.save(j) } })
            json.put(PHONES, phones.mapToJson { it.toJson { j, p -> p.save(j) } })
            json.put(URLS, urls.mapToJson { it })
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            name = PersonName.createBy(json.optJSONObject(PERSON) ?: JSONObject())
            organization = json.optString(ORGANIZATION) ?: ""
            title = json.optString(TITLE) ?: ""
            addresses.clear()
            addresses.fromJsonByObject(json, ADDRESSES) { obj -> Address.createBy(obj) }
            emails.clear()
            emails.fromJsonByObject(json, EMAILS) { obj -> Email.createBy(obj) }
            phones.clear()
            phones.fromJsonByObject(json, PHONES) { obj -> Phone.createBy(obj) }
            urls.clear()
            urls.fromJson(json, URLS) { obj -> obj.toString() }
        }

        override fun buildBarcodeValue(): String {
            val info = this
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
            return vCard.toString()
        }

    }


    class DriverLicense : BarcodeInfo() {

        companion object {
            private const val ADDRESS_CITY = "addressCity"
            private const val ADDRESS_STATE = "addressState"
            private const val ADDRESS_STREET = "addressStreet"
            private const val ADDRESS_ZIP = "addressZip"
            private const val BIRTH_DATE = "birthDate"
            private const val DOCUMENT_TYPE = "documentType"
            private const val EXPIRY_DATE = "expiryDate"
            private const val FIRST_NAME = "firstName"
            private const val GENDER = "gender"
            private const val ISSUE_DATE = "issueDate"
            private const val ISSUING_COUNTRY = "issuingCountry"
            private const val LAST_NAME = "lastName"
            private const val LICENSE_NUMBER = "licenseNumber"
            private const val MIDDLE_NAME = "middleName"
        }

        var addressCity: String = ""
        var addressState: String = ""
        var addressStreet: String = ""
        var addressZip: String = ""
        var birthDate: String = ""
        var documentType: String = ""
        var expiryDate: String = ""
        var firstName: String = ""
        var gender: String = ""
        var issueDate: String = ""
        var issuingCountry: String = ""
        var lastName: String = ""
        var licenseNumber: String = ""
        var middleName: String = ""

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(ADDRESS_CITY, addressCity)
            json.put(ADDRESS_STATE, addressState)
            json.put(ADDRESS_STREET, addressStreet)
            json.put(ADDRESS_ZIP, addressZip)
            json.put(BIRTH_DATE, birthDate)
            json.put(DOCUMENT_TYPE, documentType)
            json.put(EXPIRY_DATE, expiryDate)
            json.put(FIRST_NAME, firstName)
            json.put(GENDER, gender)
            json.put(ISSUE_DATE, issueDate)
            json.put(ISSUING_COUNTRY, issuingCountry)
            json.put(LAST_NAME, lastName)
            json.put(LICENSE_NUMBER, licenseNumber)
            json.put(MIDDLE_NAME, middleName)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            addressCity = json.optString(ADDRESS_CITY) ?: ""
            addressState = json.optString(ADDRESS_STATE) ?: ""
            addressStreet = json.optString(ADDRESS_STREET) ?: ""
            addressZip = json.optString(ADDRESS_ZIP) ?: ""
            birthDate = json.optString(BIRTH_DATE) ?: ""
            documentType = json.optString(DOCUMENT_TYPE) ?: ""
            expiryDate = json.optString(EXPIRY_DATE) ?: ""
            firstName = json.optString(FIRST_NAME) ?: ""
            gender = json.optString(GENDER) ?: ""
            issueDate = json.optString(ISSUE_DATE) ?: ""
            issuingCountry = json.optString(ISSUING_COUNTRY) ?: ""
            lastName = json.optString(LAST_NAME) ?: ""
            licenseNumber = json.optString(LICENSE_NUMBER) ?: ""
            middleName = json.optString(MIDDLE_NAME) ?: ""
        }

        override fun buildBarcodeValue(): String {
            // TODO 不正确的序列化方法
            return toJson()
        }

    }


    class CalendarEvent : BarcodeInfo() {

        companion object {
            private const val END = "end"
            private const val START = "start"
            private const val DESCRIPTION = "description"
            private const val LOCATION = "location"
            private const val ORGANIZER = "organizer"
            private const val STATUS = "status"
            private const val SUMMARY = "summary"
        }

        var end: CalendarDateTime = CalendarDateTime()
        var start: CalendarDateTime = CalendarDateTime()
        var description: String = ""
        var location: String = ""
        var organizer: String = ""
        var status: String = ""
        var summary: String = ""

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(END, end.toJson { j, c -> c.save(j) })
            json.put(START, start.toJson { j, c -> c.save(j) })
            json.put(DESCRIPTION, description)
            json.put(LOCATION, location)
            json.put(ORGANIZER, organizer)
            json.put(STATUS, status)
            json.put(SUMMARY, summary)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            end = CalendarDateTime.createBy(json.optJSONObject(END) ?: JSONObject())
            start = CalendarDateTime.createBy(json.optJSONObject(START) ?: JSONObject())
            description = json.optString(DESCRIPTION)
            location = json.optString(LOCATION)
            organizer = json.optString(ORGANIZER)
            status = json.optString(STATUS)
            summary = json.optString(SUMMARY)
        }

        override fun buildBarcodeValue(): String {
            val builder = StringBuilder()
            builder.append("BEGIN:VCALENDAR\n")
            builder.append("VERSION:2.0\n")
            builder.append("BEGIN:VEVENT\n")
            builder.append("SUMMARY:").append(summary).append("\n")
            builder.append("DESCRIPTION:").append(description).append("\n")
            builder.append("STATUS:").append(status).append("\n")
            builder.append("ORGANIZER:").append(organizer).append("\n")
            builder.append("LOCATION:").append(location).append("\n")
            builder.append("DTSTART:").append(start.formatValue()).append("\n")
            builder.append("DTEND:").append(end.formatValue()).append("\n")
            builder.append("END:VEVENT\n")
            builder.append("END:VCALENDAR")
            return builder.toString()
        }
    }

    class Email : BarcodeInfo() {

        companion object {
            internal fun createBy(json: JSONObject): Email {
                val email = Email()
                email.resume(json)
                return email
            }

            private const val TYPE = "type"
            private const val ADDRESS = "address"
            private const val BODY = "body"
            private const val SUBJECT = "subject"

        }

        var type: Type = Type.UNKNOWN
        var address: String = ""
        var body: String = ""
        var subject: String = ""

        enum class Type(override val key: Int = 0, val proto: String, val contacts: Int) : KeyEnum {
            UNKNOWN(
                Barcode.Email.TYPE_UNKNOWN,
                "",
                ContactsContract.CommonDataKinds.Email.TYPE_HOME
            ),
            WORK(
                Barcode.Email.TYPE_WORK,
                "WORK",
                ContactsContract.CommonDataKinds.Email.TYPE_WORK
            ),
            HOME(
                Barcode.Email.TYPE_HOME,
                "HOME",
                ContactsContract.CommonDataKinds.Email.TYPE_HOME
            ),
        }

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(TYPE, type.proto)
            json.put(ADDRESS, address)
            json.put(BODY, body)
            json.put(SUBJECT, subject)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            val typeProto = json.optString(TYPE) ?: ""
            type = Type.entries.find { it.proto == typeProto } ?: Type.UNKNOWN
            address = json.optString(ADDRESS)
            body = json.optString(BODY)
            subject = json.optString(SUBJECT)
        }

        /**
         * mailto:AAAA?subject=BBBB&body=CCCCC
         */
        override fun buildBarcodeValue(): String {
            val info = this
            val address = info.address.encode()
            val subject = info.subject.encode()
            val body = info.body.encode()
            val type = info.type.proto
            return "mailto:${address}?subject=${subject}&body=${body}&type=${type}"
        }

    }

    class GeoPoint : BarcodeInfo() {

        companion object {
            private const val LAT = "lat"
            private const val LNG = "lng"
        }

        var lat: Double = 0.0
        var lng: Double = 0.0

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(LAT, lat)
            json.put(LNG, lng)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            lat = json.optDouble(LAT, 0.0)
            lng = json.optDouble(LNG, 0.0)
        }

        /**
         * geo:123.456,789.123?q=123.456,789.123
         */
        override fun buildBarcodeValue(): String {
            return "geo:${lat},${lng}"
        }

    }

    class Phone : BarcodeInfo() {

        companion object {
            internal fun createBy(json: JSONObject): Phone {
                val phone = Phone()
                phone.resume(json)
                return phone
            }

            private const val TYPE = "type"
            private const val NUMBER = "number"

        }

        var type: Type = Type.UNKNOWN
        var number: String = ""

        enum class Type(override val key: Int = 0, val proto: String, val contacts: Int) : KeyEnum {
            UNKNOWN(
                Barcode.Phone.TYPE_UNKNOWN,
                "",
                ContactsContract.CommonDataKinds.Phone.TYPE_HOME
            ),
            WORK(
                Barcode.Phone.TYPE_WORK,
                "WORK",
                ContactsContract.CommonDataKinds.Phone.TYPE_WORK
            ),
            HOME(
                Barcode.Phone.TYPE_HOME,
                "HOME",
                ContactsContract.CommonDataKinds.Phone.TYPE_HOME
            ),
            FAX(
                Barcode.Phone.TYPE_FAX,
                "FAX",
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK
            ),
            MOBILE(
                Barcode.Phone.TYPE_MOBILE,
                "MOBILE",
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
            ),
        }

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(TYPE, type.proto)
            json.put(NUMBER, number)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            val typeProto = json.optString(TYPE) ?: ""
            type = Type.entries.find { it.proto == typeProto } ?: Type.UNKNOWN
            number = json.optString(NUMBER)
        }

        /**
         * tel:123456678
         */
        override fun buildBarcodeValue(): String {
            return "tel:${number.encode()}"
        }
    }

    class Sms : BarcodeInfo() {

        companion object {
            private const val MESSAGE = "message"
            private const val PHONE_NUMBER = "phoneNumber"
        }

        var message: String = ""
        var phoneNumber: String = ""

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(MESSAGE, message)
            json.put(PHONE_NUMBER, phoneNumber)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            message = json.optString(MESSAGE) ?: ""
            phoneNumber = json.optString(PHONE_NUMBER) ?: ""
        }

        /**
         * smsto:1111:AAAAAA
         */
        override fun buildBarcodeValue(): String {
            val number = phoneNumber.encode()
            val message = message.encode()
            return "smsto:${number}:${message}"
        }
    }

    class Url : BarcodeInfo() {
        companion object {
            private const val TITLE = "title"
            private const val URL = "url"
        }

        var title: String = ""
        var url: String = ""

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(TITLE, title)
            json.put(URL, url)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            title = json.optString(TITLE) ?: ""
            url = json.optString(URL) ?: ""
        }

        override fun buildBarcodeValue(): String {
            return url
        }
    }

    class Wifi : BarcodeInfo() {

        companion object {
            private const val ENCRYPTION_TYPE = "encryptionType"
            private const val PASSWORD = "password"
            private const val SSID = "ssid"
            private const val USERNAME = "username"
        }

        var encryptionType: EncryptionType = EncryptionType.OPEN
        var password: String = ""
        var ssid: String = ""
        var username: String = ""

        enum class EncryptionType(override val key: Int = 0, val proto: String) : KeyEnum {
            OPEN(Barcode.WiFi.TYPE_OPEN, ""),
            WEP(Barcode.WiFi.TYPE_WEP, "WEP"),
            WPA(Barcode.WiFi.TYPE_WPA, "WPA"),
        }

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(ENCRYPTION_TYPE, encryptionType.proto)
            json.put(PASSWORD, password)
            json.put(SSID, ssid)
            json.put(USERNAME, username)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            val typeProto = json.optString(ENCRYPTION_TYPE) ?: ""
            encryptionType =
                EncryptionType.entries.find { it.proto == typeProto } ?: EncryptionType.OPEN
            password = json.optString(PASSWORD) ?: ""
            ssid = json.optString(SSID) ?: ""
            username = json.optString(USERNAME) ?: ""
        }

        /**
         * WIFI:T:WEP;S:AAAA;P:CCCCC;I:BBBB;H:true;
         */
        override fun buildBarcodeValue(): String {
            val info = this
            val type = info.encryptionType.proto
            val ssid = info.ssid.encode()
            val pwd = info.password.encode()
            val name = info.username.encode()
            return "WIFI:T:${type};S:${ssid};P:${pwd};I:${name};H:true;;"
        }

    }

    class PersonName(
        val first: String = "",
        val formattedName: String = "",
        val last: String = "",
        val middle: String = "",
        val prefix: String = "",
        val pronunciation: String = "",
        val suffix: String = "",
    ) {

        companion object {
            private const val FIRST = "first"
            private const val FORMATTED_NAME = "formattedName"
            private const val LAST = "last"
            private const val MIDDLE = "middle"
            private const val PREFIX = "prefix"
            private const val PRONUNCIATION = "pronunciation"
            private const val SUFFIX = "suffix"

            internal fun createBy(json: JSONObject): PersonName {
                return PersonName(
                    first = json.optString(FIRST) ?: "",
                    formattedName = json.optString(FORMATTED_NAME) ?: "",
                    last = json.optString(LAST) ?: "",
                    middle = json.optString(MIDDLE) ?: "",
                    prefix = json.optString(PREFIX) ?: "",
                    pronunciation = json.optString(PRONUNCIATION) ?: "",
                    suffix = json.optString(SUFFIX) ?: "",
                )
            }

        }

        fun getDisplayValue(): String {
            return if (formattedName.isNotEmpty()) {
                formattedName
            } else {
                val builder = StringBuilder()
                    .appendByNotEmpty(prefix)
                    .appendByNotEmpty(first)
                    .appendByNotEmpty(middle)
                    .appendByNotEmpty(last)
                    .appendByNotEmpty(suffix)
                if (pronunciation.isNotEmpty()) {
                    builder.append("[").append(pronunciation).append("]")
                }
                builder.toString()
            }
        }

        private fun StringBuilder.appendByNotEmpty(value: String): StringBuilder {
            val builder = this
            if (value.isNotEmpty()) {
                if (builder.isNotEmpty()) {
                    builder.append(" ")
                }
                builder.append(value)
            }
            return builder
        }

        internal fun save(json: JSONObject) {
            json.put(FIRST, first)
            json.put(FORMATTED_NAME, formattedName)
            json.put(LAST, last)
            json.put(MIDDLE, middle)
            json.put(PREFIX, prefix)
            json.put(PRONUNCIATION, pronunciation)
            json.put(SUFFIX, suffix)
        }

    }

    class Address(
        val type: Type = Type.UNKNOWN,
        val lines: Array<String> = emptyArray(),
    ) {

        companion object {
            internal fun createBy(jsonObject: JSONObject): Address {
                val typeProto = jsonObject.optString(TYPE) ?: ""
                val linesArray = jsonObject.optJSONArray(LINES) ?: JSONArray()
                return Address(
                    type = Type.entries.find { it.proto == typeProto } ?: Type.UNKNOWN,
                    lines = Array(linesArray.length()) { linesArray.optString(it) ?: "" }
                )
            }

            private const val TYPE = "type"
            private const val LINES = "lines"

        }

        enum class Type(override val key: Int, val proto: String) : KeyEnum {
            UNKNOWN(Barcode.Address.TYPE_UNKNOWN, ""),
            WORK(Barcode.Address.TYPE_WORK, "WORK"),
            HOME(Barcode.Address.TYPE_HOME, "HOME"),
        }

        internal fun save(json: JSONObject) {
            json.put(TYPE, type.proto)
            json.put(LINES, JSONArray().apply {
                lines.forEach {
                    put(it)
                }
            })
        }

    }

    class CalendarDateTime(
        val year: Int = 0,
        val month: Int = 0,
        val day: Int = 0,
        val hours: Int = 0,
        val minutes: Int = 0,
        val seconds: Int = 0,
        val rawValue: String = "",
    ) {

        companion object {
            internal fun createBy(jsonObject: JSONObject): CalendarDateTime {
                return CalendarDateTime(
                    year = jsonObject.optInt(YEAR, 0),
                    month = jsonObject.optInt(MONTH, 0),
                    day = jsonObject.optInt(DAY, 0),
                    hours = jsonObject.optInt(HOURS, 0),
                    minutes = jsonObject.optInt(MINUTES, 0),
                    seconds = jsonObject.optInt(SECONDS, 0),
                    rawValue = jsonObject.optString(RAW_VALUE) ?: "",
                )
            }

            private const val YEAR = "year"
            private const val MONTH = "month"
            private const val DAY = "day"
            private const val HOURS = "hours"
            private const val MINUTES = "minutes"
            private const val SECONDS = "seconds"
            private const val RAW_VALUE = "rawValue"

        }

        fun formatValue(): String {
            val builder = StringBuilder()
            builder.append(year)
            builder.addNumber(month)
            builder.addNumber(day)
            builder.append("T")
            builder.addNumber(hours)
            builder.addNumber(minutes)
            builder.addNumber(seconds)
            return builder.toString()
        }

        private fun StringBuilder.addNumber(number: Int) {
            if (number < 10) {
                append("0")
            }
            append(number)
        }

        internal fun save(json: JSONObject) {
            json.put(YEAR, year)
            json.put(MONTH, month)
            json.put(DAY, day)
            json.put(HOURS, hours)
            json.put(MINUTES, minutes)
            json.put(SECONDS, seconds)
            json.put(RAW_VALUE, rawValue)
        }

    }

    interface KeyEnum {
        val key: Int
    }

}

class CodeDescribe(
    val boundingBox: Rect,
    val cornerPoints: Array<Point>,
    val displayValue: String,
    val format: BarcodeFormat,
    val bytes: ByteArray,
)

class BarcodeWrapper(
    val info: BarcodeInfo,
    val describe: CodeDescribe,
)

class BarcodeResult(
    val list: List<BarcodeWrapper>,
    val info: InputImageInfo,
    val tag: String,
) {

    val isEmpty: Boolean
        get() {
            return list.isEmpty()
        }


}

class InputImageInfo(
    val width: Int,
    val height: Int,
) {

    companion object {
        @JvmStatic
        fun from(image: InputImage): InputImageInfo {
            return InputImageInfo(image.width, image.height)
        }
    }

    fun getSize(): Size {
        return Size(width, height)
    }

}

internal object BarcodeResultBuilder {

    private inline fun <reified T : Any> ArrayList<T>.reset(list: List<T>) {
        this.clear()
        this.addAll(list)
    }

    fun createCodeDescribeBy(code: Barcode): CodeDescribe {
        return CodeDescribe(
            code.boundingBox ?: Rect(),
            code.cornerPoints ?: emptyArray(),
            code.displayValue ?: "",
            code.findFormat(),
            code.rawBytes ?: ByteArray(0)
        )
    }

    fun createContactBy(code: Barcode): BarcodeInfo.Contact {
        val info = code.contactInfo
        val nameInfo = info?.name
        return BarcodeInfo.Contact().apply {
            name = BarcodeInfo.PersonName(
                first = nameInfo?.first ?: "",
                formattedName = nameInfo?.formattedName ?: "",
                last = nameInfo?.last ?: "",
                middle = nameInfo?.middle ?: "",
                prefix = nameInfo?.prefix ?: "",
                pronunciation = nameInfo?.pronunciation ?: "",
                suffix = nameInfo?.suffix ?: ""
            )
            organization = info?.organization ?: ""
            title = info?.title ?: ""
            addresses.reset(formatAddress(info?.addresses))
            emails.reset(formatEmail(info?.emails))
            phones.reset(formatPhones(info?.phones))
            urls.reset(ArrayList(info?.urls ?: emptyList()))
        }.putInfoRaw(code)
    }

    fun createPhoneBy(code: Barcode): BarcodeInfo.Phone {
        return createPhoneBy(code.phone).putInfoRaw(code)
    }

    fun createEmailBy(
        code: Barcode,
    ): BarcodeInfo.Email {
        return createEmailBy(code.email).putInfoRaw(code)
    }

    fun createWifiBy(
        code: Barcode,
    ): BarcodeInfo.Wifi {
        val wifi = code.wifi
        return BarcodeInfo.Wifi().apply {
            encryptionType =
                BarcodeInfo.Wifi.EncryptionType.entries.findByCode(wifi?.encryptionType) {
                    BarcodeInfo.Wifi.EncryptionType.OPEN
                }
            password = wifi?.password ?: ""
            ssid = wifi?.ssid ?: ""
            username = ""
        }.putInfoRaw(code)
    }

    fun createUrlBy(
        code: Barcode,
    ): BarcodeInfo.Url {
        val urlInfo = code.url
        return BarcodeInfo.Url().apply {
            title = urlInfo?.title ?: ""
            url = urlInfo?.url ?: ""
        }.putInfoRaw(code)
    }

    fun createSmsBy(
        code: Barcode,
    ): BarcodeInfo.Sms {
        val sms = code.sms
        return BarcodeInfo.Sms().apply {
            message = sms?.message ?: ""
            phoneNumber = sms?.phoneNumber ?: ""
        }.putInfoRaw(code)
    }

    fun createGeoBy(
        code: Barcode,
    ): BarcodeInfo.GeoPoint {
        val geoPoint = code.geoPoint
        return BarcodeInfo.GeoPoint().apply {
            lat = geoPoint?.lat ?: 0.0
            lng = geoPoint?.lng ?: 0.0
        }.putInfoRaw(code)
    }

    fun createCalendarEventBy(
        code: Barcode,
    ): BarcodeInfo.CalendarEvent {
        val calendarEvent = code.calendarEvent
        return BarcodeInfo.CalendarEvent().apply {
            end = createCalendarDateTime(calendarEvent?.end)
            start = createCalendarDateTime(calendarEvent?.start)
            description = calendarEvent?.description ?: ""
            location = calendarEvent?.location ?: ""
            organizer = calendarEvent?.organizer ?: ""
            status = calendarEvent?.status ?: ""
            summary = calendarEvent?.summary ?: ""
        }.putInfoRaw(code)
    }

    fun createDriverLicenseBy(
        code: Barcode,
    ): BarcodeInfo.DriverLicense {
        val license = code.driverLicense
        return BarcodeInfo.DriverLicense().apply {
            addressCity = license?.addressCity ?: ""
            addressState = license?.addressState ?: ""
            addressStreet = license?.addressStreet ?: ""
            addressZip = license?.addressZip ?: ""
            birthDate = license?.birthDate ?: ""
            documentType = license?.documentType ?: ""
            expiryDate = license?.expiryDate ?: ""
            firstName = license?.firstName ?: ""
            gender = license?.gender ?: ""
            issueDate = license?.issueDate ?: ""
            issuingCountry = license?.issuingCountry ?: ""
            lastName = license?.lastName ?: ""
            licenseNumber = license?.licenseNumber ?: ""
            middleName = license?.middleName ?: ""
        }.putInfoRaw(code)
    }

    fun createUnknown(code: Barcode): BarcodeInfo.Unknown {
        return BarcodeInfo.Unknown().apply {
            value = code.rawValue ?: ""
        }.putInfoRaw(code)
    }

    fun createIsbn(code: Barcode): BarcodeInfo.Isbn {
        return BarcodeInfo.Isbn().apply {
            value = code.rawValue ?: ""
        }.putInfoRaw(code)
    }

    fun createProduct(code: Barcode): BarcodeInfo.Product {
        return BarcodeInfo.Product().apply {
            value = code.rawValue ?: ""
        }.putInfoRaw(code)
    }

    fun createText(code: Barcode): BarcodeInfo.Text {
        return BarcodeInfo.Text().apply {
            value = code.rawValue ?: ""
        }.putInfoRaw(code)
    }

    private inline fun <reified T : BarcodeInfo> T.putInfoRaw(code: Barcode): T {
        val info = this
        info.setRaw(code.rawValue ?: "", code.findFormat())
        return info
    }

    private fun createCalendarDateTime(
        value: Barcode.CalendarDateTime?,
    ): BarcodeInfo.CalendarDateTime {
        return BarcodeInfo.CalendarDateTime(
            year = value?.year ?: 0,
            month = value?.month ?: 0,
            day = value?.day ?: 0,
            hours = value?.hours ?: 0,
            minutes = value?.minutes ?: 0,
            seconds = value?.seconds ?: 0,
            rawValue = value?.rawValue ?: ""
        )
    }

    private fun formatPhones(
        list: List<Barcode.Phone?>?,
    ): ArrayList<BarcodeInfo.Phone> {
        val result = ArrayList<BarcodeInfo.Phone>()
        list?.forEach {
            if (it != null) {
                result.add(createPhoneBy(it))
            }
        }
        return result
    }

    private fun createPhoneBy(phone: Barcode.Phone?): BarcodeInfo.Phone {
        return BarcodeInfo.Phone().apply {
            type = BarcodeInfo.Phone.Type.entries.findByCode(phone?.type) {
                BarcodeInfo.Phone.Type.UNKNOWN
            }
            number = phone?.number ?: ""
        }
    }

    private fun formatEmail(
        list: List<Barcode.Email?>?,
    ): ArrayList<BarcodeInfo.Email> {
        val result = ArrayList<BarcodeInfo.Email>()
        list?.forEach {
            if (it != null) {
                result.add(createEmailBy(it))
            }
        }
        return result
    }

    private fun createEmailBy(
        email: Barcode.Email?,
    ): BarcodeInfo.Email {
        return BarcodeInfo.Email().apply {
            type = BarcodeInfo.Email.Type.entries.findByCode(email?.type) {
                BarcodeInfo.Email.Type.UNKNOWN
            }
            address = email?.address ?: ""
            body = email?.body ?: ""
            subject = email?.subject ?: ""
        }
    }

    private fun formatAddress(list: List<Barcode.Address?>?): ArrayList<BarcodeInfo.Address> {
        val result = ArrayList<BarcodeInfo.Address>()
        list?.forEach {
            if (it != null) {
                result.add(
                    BarcodeInfo.Address(
                        BarcodeInfo.Address.Type.entries.findByCode(it.type) {
                            BarcodeInfo.Address.Type.UNKNOWN
                        },
                        it.addressLines
                    )
                )
            }
        }
        return result
    }

    private inline fun <reified T : BarcodeInfo.KeyEnum> Iterable<T>.findByCode(
        code: Int?,
        def: () -> T,
    ): T {
        code ?: return def()
        val values = this
        values.forEach {
            if (it.key == code) {
                return it
            }
        }
        return def()
    }

    private fun Barcode.findFormat(): BarcodeFormat {
        val code = this.format
        BarcodeFormat.entries.forEach {
            if (it.code == code) {
                return it
            }
        }
        return BarcodeFormat.UNKNOWN
    }
}
