package com.example.cse476.currencyconverterhw3.models.currency

import android.graphics.Bitmap

class Currency(val currencyCode: String) {
    var icon: Bitmap? = null

    fun convertToCurrency(
        currencyAmount: Double,
        convertTo: String,
        conversionMap: Map<String, Double>
    ): Double? {
        if (this.currencyCode.equals("USD")) {
            val conversionValue = conversionMap[convertTo] ?: return null
            return currencyAmount * conversionValue
        }

        val conversionValueThis = conversionMap[this.currencyCode] ?: return null
        val usdValue = currencyAmount / conversionValueThis

        if (convertTo.equals("USD"))
            return usdValue

        // Convert this currency to USD than to target currency
        val conversionValueTarget = conversionMap[convertTo] ?: return null
        return usdValue * conversionValueTarget
    }
}