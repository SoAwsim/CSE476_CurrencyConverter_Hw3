package com.example.cse476.currencyconverterhw3.xml

import com.example.cse476.currencyconverterhw3.models.currency.Currency
import com.example.cse476.currencyconverterhw3.models.currency.CurrencyBuilder
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class CurrencyXmlParser {
    companion object {
        private val SKIP_SUPPORTED_CURRENCY_TAGS = arrayOf(
            "countryCode", "countryName", "availableFrom", "availableUntil")
    }

    private val parser = CustomXmlPullParserFactory.newInstance()

    fun parseSupportedCurrencies(stream: InputStream): List<Currency> {
        this.parser.setInput(stream.reader())

        // If 0, we are reading a new currency, we should start at -2 since
        // the response is wrapped inside 2 tags
        var depth = -2
        var textParseStatus = SupportedCurrencyTextParseStatus.NONE
        var shouldSkipCurrentItem = false
        val currencyBuilder = CurrencyBuilder()
        val currencyTable: ArrayList<Currency> = arrayListOf()

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    textParseStatus = processSupportedCurrencyStartTag()
                    if (textParseStatus != SupportedCurrencyTextParseStatus.SKIPPED)
                        depth++
                }
                XmlPullParser.TEXT -> {
                    shouldSkipCurrentItem = processSupportedCurrencyText(
                        currencyBuilder, textParseStatus)
                }
                XmlPullParser.END_TAG -> {
                    if (--depth == 0 && !shouldSkipCurrentItem)
                        currencyTable.add(currencyBuilder.buildCurrency())
                }
            }
            this.parser.next()
        }

        return currencyTable.sortedBy { it.currencyName }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skipCurrentTag() {
        if (this.parser.eventType != XmlPullParser.START_TAG)
            throw IllegalStateException()

        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
            }
        }
    }

    private fun processSupportedCurrencyStartTag(): SupportedCurrencyTextParseStatus {
        val name = this.parser.name

        // We do not care about these tags just skip them
        if (SKIP_SUPPORTED_CURRENCY_TAGS.any { it.equals(name) }) {
            this.skipCurrentTag()
            return SupportedCurrencyTextParseStatus.SKIPPED
        }

        val result: SupportedCurrencyTextParseStatus = when (name) {
            "currencyCode" -> SupportedCurrencyTextParseStatus.CURRENCY_CODE
            "currencyName" -> SupportedCurrencyTextParseStatus.CURRENCY_NAME
            "status" -> SupportedCurrencyTextParseStatus.STATUS
            "icon" -> SupportedCurrencyTextParseStatus.ICON
            else -> SupportedCurrencyTextParseStatus.NONE
        }
        return result
    }

    private fun processSupportedCurrencyText(
        builder: CurrencyBuilder,
        status: SupportedCurrencyTextParseStatus
    ): Boolean {
        val text = this.parser.text

        when (status) {
            SupportedCurrencyTextParseStatus.CURRENCY_CODE -> builder.currencyCode = text
            SupportedCurrencyTextParseStatus.CURRENCY_NAME -> builder.currencyName = text
            SupportedCurrencyTextParseStatus.STATUS -> return !text.equals("AVAILABLE")
            SupportedCurrencyTextParseStatus.ICON -> builder.icon = text
            else -> { /* ignore */ }
        }

        return false
    }

    private enum class SupportedCurrencyTextParseStatus {
        NONE,
        SKIPPED,
        CURRENCY_CODE,
        CURRENCY_NAME,
        STATUS,
        ICON
    }
}