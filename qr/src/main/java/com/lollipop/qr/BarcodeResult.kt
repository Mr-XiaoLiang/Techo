package com.lollipop.qr

import android.graphics.Point
import android.graphics.Rect
import com.google.mlkit.vision.barcode.common.Barcode

sealed class BarcodeResult(
    val codeInfo: CodeInfo
) {

    class CodeInfo(
        val boundingBox: Rect,
        val cornerPoints: Array<Point>,
        val displayValue: String,
        val format: BarcodeFormat
    )

    class Raw(
        codeInfo: CodeInfo,
        val bytes: ByteArray,
    ) : BarcodeResult(codeInfo)

    class ContactInfo(
        codeInfo: CodeInfo,
        val name: PersonName,
        val organization: String,
        val title: String,
        val addresses: List<Address>,
        val emails: List<Email>,
        val phones: List<Phone>,
        val urls: List<String>
    ) : BarcodeResult(codeInfo)

    class DriverLicense(
        codeInfo: CodeInfo,
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
    ) : BarcodeResult(codeInfo)

    class CalendarEvent(
        codeInfo: CodeInfo,
        val end: CalendarDateTime,
        val start: CalendarDateTime,
        val description: String,
        val location: String,
        val organizer: String,
        val status: String,
        val summary: String
    ) : BarcodeResult(codeInfo)

    class Email(
        codeInfo: CodeInfo,
        val type: Type,
        val address: String,
        val body: String,
        val subject: String
    ) : BarcodeResult(codeInfo) {
        enum class Type(val key: Int) {
            UNKNOWN(Barcode.Email.TYPE_UNKNOWN),
            WORK(Barcode.Email.TYPE_WORK),
            HOME(Barcode.Email.TYPE_HOME),
        }
    }

    class GeoPoint(
        codeInfo: CodeInfo,
        val lat: Double,
        val lng: Double
    ) : BarcodeResult(codeInfo)

    class Phone(
        codeInfo: CodeInfo,
        val type: Type,
        val number: String
    ) : BarcodeResult(codeInfo) {
        enum class Type(val key: Int) {
            UNKNOWN(Barcode.Phone.TYPE_UNKNOWN),
            WORK(Barcode.Phone.TYPE_WORK),
            HOME(Barcode.Phone.TYPE_HOME),
            FAX(Barcode.Phone.TYPE_FAX),
            MOBILE(Barcode.Phone.TYPE_MOBILE),
        }
    }

    class Sms(
        codeInfo: CodeInfo,
        val message: String,
        val phoneNumber: String
    ) : BarcodeResult(codeInfo)

    class Url(
        codeInfo: CodeInfo,
        val title: String,
        val url: String
    ) : BarcodeResult(codeInfo)

    class Wifi(
        codeInfo: CodeInfo,
        val encryptionType: EncryptionType,
        val password: String,
        val ssid: String
    ) : BarcodeResult(codeInfo) {
        enum class EncryptionType(val key: Int) {
            OPEN(Barcode.WiFi.TYPE_OPEN),
            WEP(Barcode.WiFi.TYPE_WEP),
            WPA(Barcode.WiFi.TYPE_WPA),
        }
    }

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

        enum class Type(val key: Int) {
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

}