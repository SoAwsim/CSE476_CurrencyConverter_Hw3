package com.example.cse476.currencyconverterhw3.models.currency

import android.graphics.Bitmap

class Currency(val currencyCode: String) {
    var icon: Bitmap? = null

    fun convertToCurrency(
        currencyAmount: Double,
        convertTo: String,
        conversionMap: Map<String, Double>
    ): Double? {
        if (this.currencyCode == "USD") {
            val conversionValue = conversionMap[convertTo] ?: return null
            return currencyAmount * conversionValue
        }

        val plainThisCurrencyCode = if (this.currencyCode.startsWith('$'))
            this.currencyCode.substring(1) else
                this.currencyCode
        val conversionValueThis = conversionMap[plainThisCurrencyCode] ?: return null
        val usdValue = currencyAmount / conversionValueThis

        if (convertTo == "USD")
            return usdValue

        val plainConvertTo = if (convertTo.startsWith('$'))
            convertTo.substring(1) else
                convertTo
        // Convert this currency to USD than to target currency
        val conversionValueTarget = conversionMap[plainConvertTo] ?: return null
        return usdValue * conversionValueTarget
    }
}