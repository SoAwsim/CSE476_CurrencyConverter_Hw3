package com.example.cse476.currencyconverterhw3.models.currency

class CurrencyBuilder {
    var currencyCode: String? = null
    var currencyName: String? = null
    var icon: String? = null

    fun buildCurrency(): Currency = Currency(
        this.currencyName ?: "",
        this.icon ?: ""
    )
}
