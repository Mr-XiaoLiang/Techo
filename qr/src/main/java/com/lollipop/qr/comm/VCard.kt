package com.lollipop.qr.comm

import com.lollipop.qr.writer.TypedBarcodeWriter.Companion.encode
import kotlin.math.min

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
class VCard(val autoWrap: Boolean, val fileName: String) {

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