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
    private val _currencyField = MutableLiveData(CurrencyFieldState())
    val currencyField: LiveData<CurrencyFieldState> = this._currencyField

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = this._isLoading

    private val _currencies = MutableLiveData<List<String>>()
    val currencies: LiveData<List<String>> = this._currencies

    private val _xmlParser = CurrencyXmlParser()

    init {
        viewModelScope.launch {
            try {
                this@MainViewModel._isLoading.value = true
                this@MainViewModel._currencies.value = this@MainViewModel.fetchAvailableCurrencies()
            } finally {
                this@MainViewModel._isLoading.value = false
            }
        }
    }

    private suspend fun fetchAvailableCurrencies(): List<String> = withContext(Dispatchers.IO) {
        val connection = URL(SUPPORTED_CURRENCIES_URL).openConnection()
        connection.connect()
        val stream = connection.getInputStream()
        val result = this@MainViewModel._xmlParser.parseSupportedCurrencies(stream)
        stream.close()

        return@withContext result.map { it.currencyCode }
    }

    fun updateFromCurrency(value: Double?) {
        _currencyField.value = CurrencyFieldState(
            currencyFromNumber = value,
            currencyToNumber = null
        )
    }

    fun convertButton() {
        val fromNumber = this._currencyField.value?.currencyFromNumber
        _currencyField.value = CurrencyFieldState(
            currencyFromNumber = fromNumber,
            currencyToNumber = 20.0
        )
    }

    companion object {
        private const val SUPPORTED_CURRENCIES_URL = "https://api.currencyfreaks.com/v2.0/supported-currencies?format=xml"
    }
}