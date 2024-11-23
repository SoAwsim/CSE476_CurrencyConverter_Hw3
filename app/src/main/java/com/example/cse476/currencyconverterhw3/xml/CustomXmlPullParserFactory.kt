package com.example.cse476.currencyconverterhw3.xml

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

internal class CustomXmlPullParserFactory {
    companion object {
        @JvmStatic
        fun newInstance(): XmlPullParser {
            val factory = XmlPullParserFactory.newInstance()
            factory.setFeature(Xml.FEATURE_RELAXED, true)
            return factory.newPullParser()
        }
    }
}