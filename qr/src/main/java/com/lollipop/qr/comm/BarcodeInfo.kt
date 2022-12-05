package com.lollipop.qr.comm

import android.graphics.Point
import android.graphics.Rect
import com.google.mlkit.vision.barcode.common.Barcode
import com.lollipop.qr.BarcodeFormat

sealed class BarcodeInfo {

    data class Text(val value: String) : BarcodeInfo()

    data class Unknown(val value: String) : BarcodeInfo()

    data class Isbn(val value: String) : BarcodeInfo()

    data class Product(val value: String) : BarcodeInfo()

    data class Contact(
        val name: PersonName,
        val organization: String,
        val title: String,
        val addresses: List<Address>,
        val emails: List<Email>,
        val phones: List<Phone>,
        val urls: List<String>
    ) : BarcodeInfo()

    data class DriverLicense(
        val addressCity: String,
        val addressState: String,
        val addressStreet: String,
        val addressZip: String,
        val birthDate: String,
        val documentType: String,
        val expiryDate: String,
        val firstName: String,
        val gender: String,
        val issueDate: String,
        val issuingCountry: String,
        val lastName: String,
        val licenseNumber: String,
        val middleName: String
    ) : BarcodeInfo()

    data class CalendarEvent(
        val end: CalendarDateTime,
        val start: CalendarDateTime,
        val description: String,
        val location: String,
        val organizer: String,
        val status: String,
        val summary: String
    ) : BarcodeInfo()

    data class Email(
        val type: Type,
        val address: String,
        val body: String,
        val subject: String
    ) : BarcodeInfo() {
        enum class Type(override val key: Int, val proto: String) : KeyEnum {
            UNKNOWN(Barcode.Email.TYPE_UNKNOWN, ""),
            WORK(Barcode.Email.TYPE_WORK, "WORK"),
            HOME(Barcode.Email.TYPE_HOME, "HOME"),
        }
    }

    data class GeoPoint(
        val lat: Double,
        val lng: Double
    ) : BarcodeInfo()

    data class Phone(
        val type: Type,
        val number: String
    ) : BarcodeInfo() {
        enum class Type(override val key: Int, val proto: String) : KeyEnum {
            UNKNOWN(Barcode.Phone.TYPE_UNKNOWN, ""),
            WORK(Barcode.Phone.TYPE_WORK, "WORK"),
            HOME(Barcode.Phone.TYPE_HOME, "HOME"),
            FAX(Barcode.Phone.TYPE_FAX, "FAX"),
            MOBILE(Barcode.Phone.TYPE_MOBILE, "MOBILE"),
        }
    }

    data class Sms(
        val message: String,
        val phoneNumber: String
    ) : BarcodeInfo()

    data class Url(
        val title: String,
        val url: String
    ) : BarcodeInfo()

    data class Wifi(
        val encryptionType: EncryptionType,
        val password: String,
        val ssid: String,
        val username: String,
    ) : BarcodeInfo() {
        enum class EncryptionType(override val key: Int, val proto: String) : KeyEnum {
            OPEN(Barcode.WiFi.TYPE_OPEN, ""),
            WEP(Barcode.WiFi.TYPE_WEP, "WEP"),
            WPA(Barcode.WiFi.TYPE_WPA, "WPA"),
        }
    }

    data class PersonName(
        val first: String,
        val formattedName: String,
        val last: String,
        val middle: String,
        val prefix: String,
        val pronunciation: String,
        val suffix: String
    )

    data class Address(
        val type: Type,
        val lines: Array<String>
    ) {

        enum class Type(override val key: Int) : KeyEnum {
            UNKNOWN(Barcode.Address.TYPE_UNKNOWN),
            WORK(Barcode.Address.TYPE_WORK),
            HOME(Barcode.Address.TYPE_HOME),
        }

    }

    data class CalendarDateTime(
        val year: Int,
        val month: Int,
        val day: Int,
        val hours: Int,
        val minutes: Int,
        val seconds: Int,
        val rawValue: String
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
            urls = info?.urls ?: emptyList()
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
    ): List<BarcodeInfo.Phone> {
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
    ): List<BarcodeInfo.Email> {
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

    private fun formatAddress(list: List<Barcode.Address?>?): List<BarcodeInfo.Address> {
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
