package com.lollipop.qr

import android.graphics.Point
import android.graphics.Rect
import com.google.mlkit.vision.barcode.common.Barcode

sealed class BarcodeResult {

    object Unknown : BarcodeResult()

    class ContactInfo(
        val name: PersonName,
        val organization: String,
        val title: String,
        val addresses: List<Address>,
        val emails: List<Email>,
        val phones: List<Phone>,
        val urls: List<String>
    ) : BarcodeResult()

    class DriverLicense(
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
    ) : BarcodeResult()

    class CalendarEvent(
        val end: CalendarDateTime,
        val start: CalendarDateTime,
        val description: String,
        val location: String,
        val organizer: String,
        val status: String,
        val summary: String
    ) : BarcodeResult()

    class Email(
        val type: Type,
        val address: String,
        val body: String,
        val subject: String
    ) : BarcodeResult() {
        enum class Type(override val key: Int) : KeyEnum {
            UNKNOWN(Barcode.Email.TYPE_UNKNOWN),
            WORK(Barcode.Email.TYPE_WORK),
            HOME(Barcode.Email.TYPE_HOME),
        }
    }

    class GeoPoint(
        val lat: Double,
        val lng: Double
    ) : BarcodeResult()

    class Phone(
        val type: Type,
        val number: String
    ) : BarcodeResult() {
        enum class Type(override val key: Int) : KeyEnum {
            UNKNOWN(Barcode.Phone.TYPE_UNKNOWN),
            WORK(Barcode.Phone.TYPE_WORK),
            HOME(Barcode.Phone.TYPE_HOME),
            FAX(Barcode.Phone.TYPE_FAX),
            MOBILE(Barcode.Phone.TYPE_MOBILE),
        }
    }

    class Sms(
        val message: String,
        val phoneNumber: String
    ) : BarcodeResult()

    class Url(
        val title: String,
        val url: String
    ) : BarcodeResult()

    class Wifi(
        val encryptionType: EncryptionType,
        val password: String,
        val ssid: String
    ) : BarcodeResult() {
        enum class EncryptionType(override val key: Int) : KeyEnum {
            OPEN(Barcode.WiFi.TYPE_OPEN),
            WEP(Barcode.WiFi.TYPE_WEP),
            WPA(Barcode.WiFi.TYPE_WPA),
        }
    }

    object Isbn : BarcodeResult()

    object Text : BarcodeResult()

    object Product : BarcodeResult()

    class PersonName(
        val first: String,
        val formattedName: String,
        val last: String,
        val middle: String,
        val prefix: String,
        val pronunciation: String,
        val suffix: String
    )

    class Address(
        val type: Type,
        val lines: Array<String>
    ) {

        enum class Type(override val key: Int) : KeyEnum {
            UNKNOWN(Barcode.Address.TYPE_UNKNOWN),
            WORK(Barcode.Address.TYPE_WORK),
            HOME(Barcode.Address.TYPE_HOME),
        }

    }

    class CalendarDateTime(
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

class CodeInfo(
    val boundingBox: Rect,
    val cornerPoints: Array<Point>,
    val displayValue: String,
    val format: BarcodeFormat,
    val bytes: ByteArray,
)

class BarcodeResultWrapper(
    val result: BarcodeResult,
    val codeInfo: CodeInfo
)

internal object BarcodeResultBuilder {
    fun createCodeInfoBy(code: Barcode): CodeInfo {
        return CodeInfo(
            code.boundingBox ?: Rect(),
            code.cornerPoints ?: emptyArray(),
            code.displayValue ?: "",
            code.findFormat(),
            code.rawBytes ?: ByteArray(0)
        )
    }

    fun createContactInfoBy(code: Barcode): BarcodeResult.ContactInfo {
        val info = code.contactInfo
        val name = info?.name
        return BarcodeResult.ContactInfo(
            name = BarcodeResult.PersonName(
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

    fun createPhoneBy(code: Barcode): BarcodeResult.Phone {
        return createPhoneBy(code.phone)
    }

    fun createEmailBy(
        code: Barcode,
    ): BarcodeResult.Email {
        return createEmailBy(code.email)
    }

    fun createWifiBy(
        code: Barcode
    ): BarcodeResult.Wifi {
        val wifi = code.wifi
        return BarcodeResult.Wifi(
            encryptionType = BarcodeResult.Wifi.EncryptionType.values()
                .findByCode(wifi?.encryptionType) {
                    BarcodeResult.Wifi.EncryptionType.OPEN
                },
            password = wifi?.password ?: "",
            ssid = wifi?.ssid ?: ""
        )
    }

    fun createUrlBy(
        code: Barcode
    ): BarcodeResult.Url {
        val url = code.url
        return BarcodeResult.Url(
            title = url?.title ?: "",
            url = url?.url ?: ""
        )
    }

    fun createSmsBy(
        code: Barcode
    ): BarcodeResult.Sms {
        val sms = code.sms
        return BarcodeResult.Sms(
            message = sms?.message ?: "",
            phoneNumber = sms?.phoneNumber ?: ""
        )
    }

    fun createGeoBy(
        code: Barcode
    ): BarcodeResult.GeoPoint {
        val geoPoint = code.geoPoint
        return BarcodeResult.GeoPoint(
            lat = geoPoint?.lat ?: 0.0,
            lng = geoPoint?.lng ?: 0.0
        )
    }

    fun createCalendarEventBy(
        code: Barcode
    ): BarcodeResult.CalendarEvent {
        val calendarEvent = code.calendarEvent
        return BarcodeResult.CalendarEvent(
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
    ): BarcodeResult.DriverLicense {
        val license = code.driverLicense
        return BarcodeResult.DriverLicense(
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
    ): BarcodeResult.CalendarDateTime {
        return BarcodeResult.CalendarDateTime(
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
    ): List<BarcodeResult.Phone> {
        val result = ArrayList<BarcodeResult.Phone>()
        list?.forEach {
            if (it != null) {
                result.add(createPhoneBy(it))
            }
        }
        return result
    }

    private fun createPhoneBy(phone: Barcode.Phone?): BarcodeResult.Phone {
        return BarcodeResult.Phone(
            type = BarcodeResult.Phone.Type.values().findByCode(phone?.type) {
                BarcodeResult.Phone.Type.UNKNOWN
            },
            number = phone?.number ?: ""
        )
    }

    private fun formatEmail(
        list: List<Barcode.Email?>?,
    ): List<BarcodeResult.Email> {
        val result = ArrayList<BarcodeResult.Email>()
        list?.forEach {
            if (it != null) {
                result.add(createEmailBy(it))
            }
        }
        return result
    }

    private fun createEmailBy(
        email: Barcode.Email?,
    ): BarcodeResult.Email {
        return BarcodeResult.Email(
            type = BarcodeResult.Email.Type.values().findByCode(email?.type) {
                BarcodeResult.Email.Type.UNKNOWN
            },
            address = email?.address ?: "",
            body = email?.body ?: "",
            subject = email?.subject ?: ""
        )
    }

    private fun formatAddress(list: List<Barcode.Address?>?): List<BarcodeResult.Address> {
        val result = ArrayList<BarcodeResult.Address>()
        list?.forEach {
            if (it != null) {
                result.add(
                    BarcodeResult.Address(
                        BarcodeResult.Address.Type.values()
                            .findByCode(it.type) { BarcodeResult.Address.Type.UNKNOWN },
                        it.addressLines
                    )
                )
            }
        }
        return result
    }

    private inline fun <reified T : BarcodeResult.KeyEnum> Array<T>.findByCode(
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
