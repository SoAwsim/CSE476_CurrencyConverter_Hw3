package com.example.cse476.currencyconverterhw3.xml

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class UsdConversionRateParser {
    private val parser = CustomXmlPullParserFactory.newInstance()

    suspend fun parseUsdConversionRates(
        stream: InputStream
    ): Map<String, Double> = withContext(Dispatchers.IO) {
        this@UsdConversionRateParser.parser.setInput(stream.reader())

        var currentCurrency: String? = null
        val rateMap: HashMap<String, Double> = hashMapOf()

        while (this@UsdConversionRateParser.parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> currentCurrency = processStartTag()
                XmlPullParser.TEXT -> processText(currentCurrency, rateMap)
                XmlPullParser.END_TAG -> currentCurrency = null
            }
            this@UsdConversionRateParser.parser.next()
        }

        this@UsdConversionRateParser.parser.setInput(null)
        return@withContext rateMap
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun processStartTag(): String? {
        val name = this.parser.name

        if (name.equals("LatestRatesResponse"))
            return null

        if (SKIP_TAGS.contains(name)) {
            this.parser.skipCurrentTag()
            return null
        }

        if (name.startsWith('_'))
            return name.substring(1).uppercase()

        return name.uppercase()
    }

    private fun processText(currentCurrency: String?, rateMap: HashMap<String, Double>) {
        if (currentCurrency == null)
            return

        val value = this.parser.text.toDoubleOrNull() ?: return

        rateMap[currentCurrency] = value
    }

    companion object {
        private val SKIP_TAGS = arrayOf(
            "date", "base"
        )
    }
}