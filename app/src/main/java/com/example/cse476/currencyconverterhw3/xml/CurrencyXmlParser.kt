package com.example.cse476.currencyconverterhw3.xml

import android.content.Context
import android.util.Log
import com.example.cse476.currencyconverterhw3.models.currency.Currency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL

class CurrencyXmlParser {
    companion object {
        const val CURRENCY_IMAGE_DIR = "currency_icons"

        private val SKIP_SUPPORTED_CURRENCY_TAGS = arrayOf(
            "countryCode", "currencyName", "countryName", "availableFrom", "availableUntil")
    }

    private val parser = CustomXmlPullParserFactory.newInstance()

    suspend fun parseSupportedCurrencies(
        stream: InputStream,
        context: Context
    ): List<Currency> = withContext(Dispatchers.IO) {
        this@CurrencyXmlParser.parser.setInput(stream.reader())

        // If 0 we are reading a new currency
        // We should start at -2 since the response is wrapped inside 2 tags
        var depth = -2
        var textParseStatus = SupportedCurrencyTextParseStatus.NONE
        var currentCurrency : Currency? = null
        var skipCurrentCurrency = false
        val currencyTable: ArrayList<Currency> = arrayListOf()
        val deferredList: ArrayList<Deferred<Unit>> = arrayListOf()

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    textParseStatus = this@CurrencyXmlParser.processSupportedCurrencyStartTag()
                    if (textParseStatus != SupportedCurrencyTextParseStatus.SKIPPED)
                        depth++
                }
                XmlPullParser.TEXT -> {
                    if (!skipCurrentCurrency) {
                        try {
                            val result = this@CurrencyXmlParser.processSupportedCurrencyText(
                                textParseStatus, context, deferredList)
                            currentCurrency = result ?: currentCurrency
                        } catch (e: SkipCurrencyInXmlException) {
                            currentCurrency = null
                            skipCurrentCurrency = true
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (--depth == 0 && currentCurrency != null) {
                        currencyTable.add(currentCurrency)
                        currentCurrency = null
                        skipCurrentCurrency = false
                    }
                }
            }
            this@CurrencyXmlParser.parser.next()
        }

        parser.setInput(null)
        val result = currencyTable.sortedBy { it.currencyCode }
        deferredList.awaitAll()
        return@withContext result
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

    @Throws(XmlPullParserException::class, IOException::class)
    private fun processSupportedCurrencyStartTag(): SupportedCurrencyTextParseStatus {
        val name = this.parser.name

        // We do not care about these tags just skip them
        if (SKIP_SUPPORTED_CURRENCY_TAGS.any { it.equals(name) }) {
            this.skipCurrentTag()
            return SupportedCurrencyTextParseStatus.SKIPPED
        }

        val result: SupportedCurrencyTextParseStatus = when (name) {
            "currencyCode" -> SupportedCurrencyTextParseStatus.CURRENCY_CODE
            "status" -> SupportedCurrencyTextParseStatus.STATUS
            "icon" -> SupportedCurrencyTextParseStatus.ICON
            else -> SupportedCurrencyTextParseStatus.NONE
        }
        return result
    }

    private fun processSupportedCurrencyText(
        status: SupportedCurrencyTextParseStatus,
        context: Context,
        deferredList: ArrayList<Deferred<Unit>>
    ): Currency? {
        val text = this.parser.text

        when (status) {
            SupportedCurrencyTextParseStatus.CURRENCY_CODE -> return Currency(text)
            SupportedCurrencyTextParseStatus.STATUS -> if (!text.equals("AVAILABLE")) throw SkipCurrencyInXmlException()
            SupportedCurrencyTextParseStatus.ICON -> deferredList.add(
                fetchCurrencyPng(text, context, CoroutineScope(Dispatchers.IO)))
            else -> { /* ignore */ }
        }

        return null
    }

    private fun fetchCurrencyPng(
        link: String,
        context: Context,
        scope: CoroutineScope
    ): Deferred<Unit> = scope.async(Dispatchers.IO) {
        val fileName = link.substring(link.lastIndexOf('/') + 1)
        val folder = File(context.filesDir, CURRENCY_IMAGE_DIR)

        if (!folder.exists())
            folder.mkdirs()

        val outputFile = File(folder, fileName)

        if (outputFile.exists())
            return@async

        try {
            val pngUrl = URL(link).openConnection()
            pngUrl.connect()

            val buffer = ByteArray(8092)
            var bytesRead: Int

            val imageStream = pngUrl.getInputStream()
            outputFile.outputStream().use { output ->
                while (imageStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
            }
        } catch (e: Exception) {
            Log.e("DOWNLOAD", "Download failed", e)
            // If an error occurs skip this image
        }
    }

    private enum class SupportedCurrencyTextParseStatus {
        NONE,
        SKIPPED,
        CURRENCY_CODE,
        STATUS,
        ICON
    }
}