package com.lollipop.qr.comm

import android.graphics.Point
import android.graphics.Rect
import com.google.mlkit.vision.barcode.common.Barcode
import com.lollipop.qr.BarcodeFormat
import org.json.JSONObject

sealed class BarcodeInfo {

    companion object {
        private const val INFO_NAME = "name"
    }

    protected inline fun <reified T : Any> T.name(): String {
        return this::class.java.name
    }

    fun toJson(): String {
        val jsonObject = JSONObject()
        try {
            save(jsonObject)
            jsonObject.put(INFO_NAME, name())
        } catch (e: Throwable) {
            e.printStackTrace()
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
                    return instance
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return Text(json)
    }

    protected open fun save(json: JSONObject) {

    }

    protected open fun resume(json: JSONObject) {

    }

    class Text @JvmOverloads constructor(var value: String = "") : BarcodeInfo() {
        companion object {
            private const val VALUE = "value"
        }

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            value = json.optString(VALUE)
        }
    }

    class Unknown @JvmOverloads constructor(var value: String = "") : BarcodeInfo() {
        companion object {
            private const val VALUE = "value"
        }

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            value = json.optString(VALUE)
        }
    }

    class Isbn @JvmOverloads constructor(var value: String = "") : BarcodeInfo() {
        companion object {
            private const val VALUE = "value"
        }

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            value = json.optString(VALUE)
        }
    }

    class Product @JvmOverloads constructor(var value: String = "") : BarcodeInfo() {
        companion object {
            private const val VALUE = "value"
        }

        override fun save(json: JSONObject) {
            super.save(json)
            json.put(VALUE, value)
        }

        override fun resume(json: JSONObject) {
            super.resume(json)
            value = json.optString(VALUE)
        }
    }

    class Contact @JvmOverloads constructor(
        val name: PersonName = PersonName(),
        var organization: String = "",
        var title: String = "",
        val addresses: ArrayList<Address> = ArrayList(),
        val emails: ArrayList<Email> = ArrayList(),
        val phones: ArrayList<Phone> = ArrayList(),
        val urls: ArrayList<String> = ArrayList()
    ) : BarcodeInfo() {

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
    ) : BarcodeInfo()

    class CalendarEvent @JvmOverloads constructor(
        var end: CalendarDateTime = CalendarDateTime(),
        var start: CalendarDateTime = CalendarDateTime(),
        var description: String = "",
        var location: String = "",
        var organizer: String = "",
        var status: String = "",
        var summary: String = ""
    ) : BarcodeInfo()

    class Email @JvmOverloads constructor(
        var type: Type = Type.UNKNOWN,
        var address: String = "",
        var body: String = "",
        var subject: String = ""
    ) : BarcodeInfo() {
        enum class Type(override val key: Int = 0, val proto: String) : KeyEnum {
            UNKNOWN(Barcode.Email.TYPE_UNKNOWN, ""),
            WORK(Barcode.Email.TYPE_WORK, "WORK"),
            HOME(Barcode.Email.TYPE_HOME, "HOME"),
        }
    }

    class GeoPoint @JvmOverloads constructor(
        var lat: Double = 0.0,
        var lng: Double = 0.0
    ) : BarcodeInfo()

    class Phone @JvmOverloads constructor(
        var type: Type = Type.UNKNOWN,
        var number: String = ""
    ) : BarcodeInfo() {
        enum class Type(override val key: Int = 0, val proto: String) : KeyEnum {
            UNKNOWN(Barcode.Phone.TYPE_UNKNOWN, ""),
            WORK(Barcode.Phone.TYPE_WORK, "WORK"),
            HOME(Barcode.Phone.TYPE_HOME, "HOME"),
            FAX(Barcode.Phone.TYPE_FAX, "FAX"),
            MOBILE(Barcode.Phone.TYPE_MOBILE, "MOBILE"),
        }
    }

    class Sms @JvmOverloads constructor(
        var message: String = "",
        var phoneNumber: String = ""
    ) : BarcodeInfo()

    class Url @JvmOverloads constructor(
        var title: String = "",
        var url: String = ""
    ) : BarcodeInfo()

    class Wifi @JvmOverloads constructor(
        var encryptionType: EncryptionType = EncryptionType.OPEN,
        var password: String = "",
        var ssid: String = "",
        var username: String = "",
    ) : BarcodeInfo() {
        enum class EncryptionType(override val key: Int = 0, val proto: String) : KeyEnum {
            OPEN(Barcode.WiFi.TYPE_OPEN, ""),
            WEP(Barcode.WiFi.TYPE_WEP, "WEP"),
            WPA(Barcode.WiFi.TYPE_WPA, "WPA"),
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
    )

    class Address(
        val type: Type = Type.UNKNOWN,
        val lines: Array<String> = emptyArray()
    ) {

        enum class Type(override val key: Int) : KeyEnum {
            UNKNOWN(Barcode.Address.TYPE_UNKNOWN),
            WORK(Barcode.Address.TYPE_WORK),
            HOME(Barcode.Address.TYPE_HOME),
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
    )

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
        )
    }

    fun createPhoneBy(code: Barcode): BarcodeInfo.Phone {
        return createPhoneBy(code.phone)
    }

    fun createEmailBy(
        code: Barcode,
    ): BarcodeInfo.Email {
        return createEmailBy(code.email)
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
        )
    }

    fun createUrlBy(
        code: Barcode
    ): BarcodeInfo.Url {
        val url = code.url
        return BarcodeInfo.Url(
            title = url?.title ?: "",
            url = url?.url ?: ""
        )
    }

    fun createSmsBy(
        code: Barcode
    ): BarcodeInfo.Sms {
        val sms = code.sms
        return BarcodeInfo.Sms(
            message = sms?.message ?: "",
            phoneNumber = sms?.phoneNumber ?: ""
        )
    }

    fun createGeoBy(
        code: Barcode
    ): BarcodeInfo.GeoPoint {
        val geoPoint = code.geoPoint
        return BarcodeInfo.GeoPoint(
            lat = geoPoint?.lat ?: 0.0,
            lng = geoPoint?.lng ?: 0.0
        )
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
        )
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
        )
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
