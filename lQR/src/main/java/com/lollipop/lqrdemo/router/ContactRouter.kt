package com.lollipop.lqrdemo.router

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import com.lollipop.qr.comm.BarcodeInfo

object ContactRouter : BarcodeRouter<BarcodeInfo.Contact>() {

    override fun getIntent(context: Context, barcodeInfo: BarcodeInfo.Contact): Intent {
        //添加需要设置的数据
        val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
        buildInfo(intent, barcodeInfo)
        return intent
    }

    private fun buildInfo(intent: Intent, barcodeInfo: BarcodeInfo.Contact) {
        intent.putExtra(
            ContactsContract.Intents.Insert.NAME,
            barcodeInfo.name.getDisplayValue()
        )
        barcodeInfo.phones.findFirst()?.let { phone ->
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone.number)
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, phone.type.contacts)
        }
        barcodeInfo.phones.findSecondary()?.let { phone ->
            intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, phone.number)
            intent.putExtra(
                ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE,
                phone.type.contacts
            )
        }
        barcodeInfo.phones.findTertiary()?.let { phone ->
            intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, phone.number)
            intent.putExtra(
                ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE,
                phone.type.contacts
            )
        }
        barcodeInfo.emails.findFirst()?.let { email ->
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email.address)
            intent.putExtra(
                ContactsContract.Intents.Insert.EMAIL_TYPE,
                email.type.contacts
            )
        }
        barcodeInfo.emails.findSecondary()?.let { email ->
            intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, email.address)
            intent.putExtra(
                ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE,
                email.type.contacts
            )
        }
        barcodeInfo.emails.findTertiary()?.let { email ->
            intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, email.address)
            intent.putExtra(
                ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE,
                email.type.contacts
            )
        }
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, barcodeInfo.organization)

        val values = ArrayList<ContentValues>()

        barcodeInfo.addresses.forEach { address ->
            values.add(ContentValues().apply {
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                )
                val linesValue = StringBuilder()
                address.lines.forEach {
                    linesValue.append(it)
                    linesValue.append(" ")
                }
                put(
                    ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                    linesValue.toString()
                )
            })
        }
        barcodeInfo.urls.forEach { url ->
            values.add(ContentValues().apply {
                put(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
                )
                put(ContactsContract.CommonDataKinds.Website.URL, url)
            })
        }
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, values)
    }

}