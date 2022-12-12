package com.lollipop.qr.comm

import android.graphics.Point
import android.graphics.Rect
import com.google.mlkit.vision.barcode.common.Barcode
import com.lollipop.qr.BarcodeFormat
import org.json.JSONArray
import org.json.JSONObject

sealed class BarcodeInfo {

    companion object {
        private const val INFO_NAME = "infoName"
        private const val INFO_RAW = "infoRaw"
    }

    protected inline fun <reified T : Any> T.name(): String {
        return this::class.java.name
    }

    var rawValue: String = ""

    fun toJson(): String {
        val jsonObject = this.toJson { json, info ->
            info.save(json)
            json.put(INFO_NAME, name())
            json.put(INFO_RAW, info.rawValue)
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
                val instance = Class.forName(name).newInstance()
                if (instance is BarcodeInfo) {
                    instance.resume(jsonObject)
                    instance.rawValue = jsonObject.optString(INFO_RAW)
                    return instance
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return Text(json)
    }

    internal abstract fun save(json: JSONObject)

    internal abstract fun resume(json: JSONObject)

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
        map: (Any) -> T
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
        map: (JSONObject) -> T
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

    class Text @JvmOverloads constructor(var value: String = "") : BarcodeInfo() {
        companion object {
            private const val VALUE = "value"
        }

        override fun save(json: JSONObject) {
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            value = json.optString(VALUE)
        }
    }

    class Unknown @JvmOverloads constructor(var value: String = "") : BarcodeInfo() {
        companion object {
            private const val VALUE = "value"
        }

        override fun save(json: JSONObject) {
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            value = json.optString(VALUE)
        }
    }

    class Isbn @JvmOverloads constructor(var value: String = "") : BarcodeInfo() {
        companion object {
            private const val VALUE = "value"
        }

        override fun save(json: JSONObject) {
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            value = json.optString(VALUE)
        }
    }

    class Product @JvmOverloads constructor(var value: String = "") : BarcodeInfo() {
        companion object {
            private const val VALUE = "value"
        }

        override fun save(json: JSONObject) {
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            value = json.optString(VALUE)
        }
    }

    class Contact @JvmOverloads constructor(
        var name: PersonName = PersonName(),
        var organization: String = "",
        var title: String = "",
        val addresses: ArrayList<Address> = ArrayList(),
        val emails: ArrayList<Email> = ArrayList(),
        val phones: ArrayList<Phone> = ArrayList(),
        val urls: ArrayList<String> = ArrayList()
    ) : BarcodeInfo() {

        companion object {
            private const val PERSON = "person"
            private const val ORGANIZATION = "organization"
            private const val TITLE = "title"
            private const val ADDRESSES = "addresses"
            private const val EMAILS = "emails"
            private const val PHONES = "phones"
            private const val URLS = "urls"
        }

        override fun save(json: JSONObject) {
            json.put(PERSON, name.toJson { j, p -> p.save(j) })
            json.put(ORGANIZATION, organization)
            json.put(TITLE, title)
            json.put(ADDRESSES, addresses.mapToJson { it.toJson { j, a -> a.save(j) } })
            json.put(EMAILS, emails.mapToJson { it.toJson { j, e -> e.save(j) } })
            json.put(PHONES, phones.mapToJson { it.toJson { j, p -> p.save(j) } })
            json.put(URLS, urls.mapToJson { it })
        }

        override fun resume(json: JSONObject) {
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

    }


    class DriverLicense @JvmOverloads constructor(
        var addressCity: String = "",
        var addressState: String = "",
        var addressStreet: String = "",
        var addressZip: String = "",
        var birthDate: String = "",
        var documentType: String = "",
        var expiryDate: String = "",
        var firstName: String = "",
        var gender: String = "",
        var issueDate: String = "",
        var issuingCountry: String = "",
        var lastName: String = "",
        var licenseNumber: String = "",
        var middleName: String = ""
    ) : BarcodeInfo() {

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

        override fun save(json: JSONObject) {
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

    }


    class CalendarEvent @JvmOverloads constructor(
        var end: CalendarDateTime = CalendarDateTime(),
        var start: CalendarDateTime = CalendarDateTime(),
        var description: String = "",
        var location: String = "",
        var organizer: String = "",
        var status: String = "",
        var summary: String = ""
    ) : BarcodeInfo() {

        companion object {
            private const val END = "end"
            private const val START = "start"
            private const val DESCRIPTION = "description"
            private const val LOCATION = "location"
            private const val ORGANIZER = "organizer"
            private const val STATUS = "status"
            private const val SUMMARY = "summary"
        }

        override fun save(json: JSONObject) {
            json.put(END, end.toJson { j, c -> c.save(j) })
            json.put(START, start.toJson { j, c -> c.save(j) })
            json.put(DESCRIPTION, description)
            json.put(LOCATION, location)
            json.put(ORGANIZER, organizer)
            json.put(STATUS, status)
            json.put(SUMMARY, summary)
        }

        override fun resume(json: JSONObject) {
            end = CalendarDateTime.createBy(json.optJSONObject(END) ?: JSONObject())
            start = CalendarDateTime.createBy(json.optJSONObject(START) ?: JSONObject())
            description = json.optString(DESCRIPTION)
            location = json.optString(LOCATION)
            organizer = json.optString(ORGANIZER)
            status = json.optString(STATUS)
            summary = json.optString(SUMMARY)
        }
    }

    class Email @JvmOverloads constructor(
        var type: Type = Type.UNKNOWN,
        var address: String = "",
        var body: String = "",
        var subject: String = ""
    ) : BarcodeInfo() {

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

        enum class Type(override val key: Int = 0, val proto: String) : KeyEnum {
            UNKNOWN(Barcode.Email.TYPE_UNKNOWN, ""),
            WORK(Barcode.Email.TYPE_WORK, "WORK"),
            HOME(Barcode.Email.TYPE_HOME, "HOME"),
        }

        override fun save(json: JSONObject) {
            json.put(TYPE, type.proto)
            json.put(ADDRESS, address)
            json.put(BODY, body)
            json.put(SUBJECT, subject)
        }

        override fun resume(json: JSONObject) {
            val typeProto = json.optString(TYPE) ?: ""
            type = Type.values().find { it.proto == typeProto } ?: Type.UNKNOWN
            address = json.optString(ADDRESS)
            body = json.optString(BODY)
            subject = json.optString(SUBJECT)
        }

    }

    class GeoPoint @JvmOverloads constructor(
        var lat: Double = 0.0,
        var lng: Double = 0.0
    ) : BarcodeInfo() {

        companion object {
            private const val LAT = "lat"
            private const val LNG = "lng"
        }

        override fun save(json: JSONObject) {
            json.put(LAT, lat)
            json.put(LNG, lng)
        }

        override fun resume(json: JSONObject) {
            lat = json.optDouble(LAT, 0.0)
            lng = json.optDouble(LNG, 0.0)
        }

    }

    class Phone @JvmOverloads constructor(
        var type: Type = Type.UNKNOWN,
        var number: String = ""
    ) : BarcodeInfo() {

        companion object {
            internal fun createBy(json: JSONObject): Phone {
                val phone = Phone()
                phone.resume(json)
                return phone
            }

            private const val TYPE = "type"
            private const val NUMBER = "number"

        }

        enum class Type(override val key: Int = 0, val proto: String) : KeyEnum {
            UNKNOWN(Barcode.Phone.TYPE_UNKNOWN, ""),
            WORK(Barcode.Phone.TYPE_WORK, "WORK"),
            HOME(Barcode.Phone.TYPE_HOME, "HOME"),
            FAX(Barcode.Phone.TYPE_FAX, "FAX"),
            MOBILE(Barcode.Phone.TYPE_MOBILE, "MOBILE"),
        }

        override fun save(json: JSONObject) {
            json.put(TYPE, type.proto)
            json.put(NUMBER, number)
        }

        override fun resume(json: JSONObject) {
            val typeProto = json.optString(TYPE) ?: ""
            type = Type.values().find { it.proto == typeProto } ?: Type.UNKNOWN
            number = json.optString(NUMBER)
        }
    }

    class Sms @JvmOverloads constructor(
        var message: String = "",
        var phoneNumber: String = ""
    ) : BarcodeInfo() {

        companion object {
            private const val MESSAGE = "message"
            private const val PHONE_NUMBER = "phoneNumber"
        }

        override fun save(json: JSONObject) {
            json.put(MESSAGE, message)
            json.put(PHONE_NUMBER, phoneNumber)
        }

        override fun resume(json: JSONObject) {
            message = json.optString(MESSAGE) ?: ""
            phoneNumber = json.optString(PHONE_NUMBER) ?: ""
        }
    }

    class Url @JvmOverloads constructor(
        var title: String = "",
        var url: String = ""
    ) : BarcodeInfo() {
        companion object {
            private const val TITLE = "title"
            private const val URL = "url"
        }

        override fun save(json: JSONObject) {
            json.put(TITLE, title)
            json.put(URL, url)
        }

        override fun resume(json: JSONObject) {
            title = json.optString(TITLE) ?: ""
            url = json.optString(URL) ?: ""
        }
    }

    class Wifi @JvmOverloads constructor(
        var encryptionType: EncryptionType = EncryptionType.OPEN,
        var password: String = "",
        var ssid: String = "",
        var username: String = "",
    ) : BarcodeInfo() {

        companion object {
            private const val ENCRYPTION_TYPE = "encryptionType"
            private const val PASSWORD = "password"
            private const val SSID = "ssid"
            private const val USERNAME = "username"
        }

        enum class EncryptionType(override val key: Int = 0, val proto: String) : KeyEnum {
            OPEN(Barcode.WiFi.TYPE_OPEN, ""),
            WEP(Barcode.WiFi.TYPE_WEP, "WEP"),
            WPA(Barcode.WiFi.TYPE_WPA, "WPA"),
        }

        override fun save(json: JSONObject) {
            json.put(ENCRYPTION_TYPE, encryptionType.proto)
            json.put(PASSWORD, password)
            json.put(SSID, ssid)
            json.put(USERNAME, username)
        }

        override fun resume(json: JSONObject) {
            val typeProto = json.optString(ENCRYPTION_TYPE) ?: ""
            encryptionType =
                EncryptionType.values().find { it.proto == typeProto } ?: EncryptionType.OPEN
            password = json.optString(PASSWORD) ?: ""
            ssid = json.optString(SSID) ?: ""
            username = json.optString(USERNAME) ?: ""
        }

    }

    class PersonName(
        val first: String = "",
        val formattedName: String = "",
        val last: String = "",
        val middle: String = "",
        val prefix: String = "",
        val pronunciation: String = "",
        val suffix: String = ""
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
        val lines: Array<String> = emptyArray()
    ) {

        companion object {
            internal fun createBy(jsonObject: JSONObject): Address {
                val typeProto = jsonObject.optString(TYPE) ?: ""
                val linesArray = jsonObject.optJSONArray(LINES) ?: JSONArray()
                return Address(
                    type = Type.values().find { it.proto == typeProto } ?: Type.UNKNOWN,
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
        val rawValue: String = ""
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
    val describe: CodeDescribe
)

class BarcodeResult(
    val list: List<BarcodeWrapper>,
    val tag: String
) {

    val isEmpty: Boolean
        get() {
            return list.isEmpty()
        }


}

internal object BarcodeResultBuilder {
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
        val name = info?.name
        return BarcodeInfo.Contact(
            name = BarcodeInfo.PersonName(
                first = name?.first ?: "",
                formattedName = name?.formattedName ?: "",
                last = name?.last ?: "",
                middle = name?.middle ?: "",
                prefix = name?.prefix ?: "",
                pronunciation = name?.pronunciation ?: "",
                suffix = name?.suffix ?: ""
            ),
            organization = info?.organization ?: "",
            title = info?.title ?: "",
            addresses = formatAddress(info?.addresses),
            emails = formatEmail(info?.emails),
            phones = formatPhones(info?.phones),
            urls = ArrayList(info?.urls ?: emptyList())
        ).putInfoRaw(code)
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
        code: Barcode
    ): BarcodeInfo.Wifi {
        val wifi = code.wifi
        return BarcodeInfo.Wifi(
            encryptionType = BarcodeInfo.Wifi.EncryptionType.values()
                .findByCode(wifi?.encryptionType) {
                    BarcodeInfo.Wifi.EncryptionType.OPEN
                },
            password = wifi?.password ?: "",
            ssid = wifi?.ssid ?: "",
            username = ""
        ).putInfoRaw(code)
    }

    fun createUrlBy(
        code: Barcode
    ): BarcodeInfo.Url {
        val url = code.url
        return BarcodeInfo.Url(
            title = url?.title ?: "",
            url = url?.url ?: ""
        ).putInfoRaw(code)
    }

    fun createSmsBy(
        code: Barcode
    ): BarcodeInfo.Sms {
        val sms = code.sms
        return BarcodeInfo.Sms(
            message = sms?.message ?: "",
            phoneNumber = sms?.phoneNumber ?: ""
        ).putInfoRaw(code)
    }

    fun createGeoBy(
        code: Barcode
    ): BarcodeInfo.GeoPoint {
        val geoPoint = code.geoPoint
        return BarcodeInfo.GeoPoint(
            lat = geoPoint?.lat ?: 0.0,
            lng = geoPoint?.lng ?: 0.0
        ).putInfoRaw(code)
    }

    fun createCalendarEventBy(
        code: Barcode
    ): BarcodeInfo.CalendarEvent {
        val calendarEvent = code.calendarEvent
        return BarcodeInfo.CalendarEvent(
            end = createCalendarDateTime(calendarEvent?.end),
            start = createCalendarDateTime(calendarEvent?.start),
            description = calendarEvent?.description ?: "",
            location = calendarEvent?.location ?: "",
            organizer = calendarEvent?.organizer ?: "",
            status = calendarEvent?.status ?: "",
            summary = calendarEvent?.summary ?: ""
        ).putInfoRaw(code)
    }

    fun createDriverLicenseBy(
        code: Barcode
    ): BarcodeInfo.DriverLicense {
        val license = code.driverLicense
        return BarcodeInfo.DriverLicense(
            addressCity = license?.addressCity ?: "",
            addressState = license?.addressState ?: "",
            addressStreet = license?.addressStreet ?: "",
            addressZip = license?.addressZip ?: "",
            birthDate = license?.birthDate ?: "",
            documentType = license?.documentType ?: "",
            expiryDate = license?.expiryDate ?: "",
            firstName = license?.firstName ?: "",
            gender = license?.gender ?: "",
            issueDate = license?.issueDate ?: "",
            issuingCountry = license?.issuingCountry ?: "",
            lastName = license?.lastName ?: "",
            licenseNumber = license?.licenseNumber ?: "",
            middleName = license?.middleName ?: ""
        ).putInfoRaw(code)
    }

    fun createUnknown(code: Barcode): BarcodeInfo.Unknown {
        return BarcodeInfo.Unknown(code.rawValue ?: "").putInfoRaw(code)
    }

    fun createIsbn(code: Barcode): BarcodeInfo.Isbn {
        return BarcodeInfo.Isbn(code.displayValue ?: "").putInfoRaw(code)
    }

    fun createProduct(code: Barcode): BarcodeInfo.Product {
        return BarcodeInfo.Product(code.displayValue ?: "").putInfoRaw(code)
    }

    fun createText(code: Barcode): BarcodeInfo.Text {
        return BarcodeInfo.Text(code.displayValue ?: "").putInfoRaw(code)
    }

    private inline fun <reified T : BarcodeInfo> T.putInfoRaw(code: Barcode): T {
        val info = this
        info.rawValue = code.rawValue ?: ""
        return info
    }

    private fun createCalendarDateTime(
        value: Barcode.CalendarDateTime?
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
        list: List<Barcode.Phone?>?
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
        return BarcodeInfo.Phone(
            type = BarcodeInfo.Phone.Type.values().findByCode(phone?.type) {
                BarcodeInfo.Phone.Type.UNKNOWN
            },
            number = phone?.number ?: ""
        )
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
        return BarcodeInfo.Email(
            type = BarcodeInfo.Email.Type.values().findByCode(email?.type) {
                BarcodeInfo.Email.Type.UNKNOWN
            },
            address = email?.address ?: "",
            body = email?.body ?: "",
            subject = email?.subject ?: ""
        )
    }

    private fun formatAddress(list: List<Barcode.Address?>?): ArrayList<BarcodeInfo.Address> {
        val result = ArrayList<BarcodeInfo.Address>()
        list?.forEach {
            if (it != null) {
                result.add(
                    BarcodeInfo.Address(
                        BarcodeInfo.Address.Type.values()
                            .findByCode(it.type) { BarcodeInfo.Address.Type.UNKNOWN },
                        it.addressLines
                    )
                )
            }
        }
        return result
    }

    private inline fun <reified T : BarcodeInfo.KeyEnum> Array<T>.findByCode(
        code: Int?,
        def: () -> T
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

    fun Barcode.findFormat(): BarcodeFormat {
        val code = this.format
        BarcodeFormat.values().forEach {
            if (it.code == code) {
                return it
            }
        }
        return BarcodeFormat.UNKNOWN
    }
}
