package com.example.cse476.currencyconverterhw3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cse476.currencyconverterhw3.xml.CurrencyXmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

data class CurrencyFieldState(
    val currencyFromNumber: Double? = null,
    val currencyToNumber: Double? = null
)

class MainViewModel: ViewModel() {
    private val _currencyFieldState = MutableLiveData(CurrencyFieldState())
    val currencyFieldState: LiveData<CurrencyFieldState> = _currencyFieldState

    private val _xmlParser = CurrencyXmlParser()

    init {
        viewModelScope.launch {
            this@MainViewModel.fetchAvailableCurrencies()
        }
    }

    private suspend fun fetchAvailableCurrencies() = withContext(Dispatchers.IO) {
        val connection = URL(SUPPORTED_CURRENCIES_URL).openConnection()
        connection.connect()
        val stream = connection.getInputStream()
        val result = this@MainViewModel._xmlParser.parseSupportedCurrencies(stream)
    }

    fun updateFromCurrency(value: Double?) {
        _currencyFieldState.value = CurrencyFieldState(
            currencyFromNumber = value,
            currencyToNumber = null
        )
    }

    fun convertButton() {
        val fromNumber = this._currencyFieldState.value?.currencyFromNumber
        _currencyFieldState.value = CurrencyFieldState(
            currencyFromNumber = fromNumber,
            currencyToNumber = 20.0
        )
    }

    companion object {
        private const val SUPPORTED_CURRENCIES_URL = "https://api.currencyfreaks.com/v2.0/supported-currencies?format=xml"
    }
}