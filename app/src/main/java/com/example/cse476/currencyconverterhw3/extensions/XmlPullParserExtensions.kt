package com.example.cse476.currencyconverterhw3.extensions

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

@Throws(XmlPullParserException::class, IOException::class)
fun XmlPullParser.skipCurrentTag() {
    if (this.eventType != XmlPullParser.START_TAG)
        throw IllegalStateException()

    var depth = 1
    while (depth != 0) {
        when (this.next()) {
            XmlPullParser.START_TAG -> depth++
            XmlPullParser.END_TAG -> depth--
        }
    }
}