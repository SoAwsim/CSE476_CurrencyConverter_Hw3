package com.example.cse476.currencyconverterhw3.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cse476.currencyconverterhw3.models.currency.Currency
import com.example.cse476.currencyconverterhw3.models.network.NetworkMonitor
import com.example.cse476.currencyconverterhw3.xml.CurrencyXmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

data class CurrencyFieldState(
    val currencyFromNumber: Double? = null,
    val currencyToNumber: Double? = null
)

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val _currencyField = MutableLiveData(CurrencyFieldState())
    val currencyField: LiveData<CurrencyFieldState> = this._currencyField

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = this._isLoading

    private val _currencies = MutableLiveData<List<Currency>>()
    val currencies: LiveData<List<Currency>> = this._currencies

    private val _xmlParser = CurrencyXmlParser()

    private val _networkMonitor =
        NetworkMonitor(this.getApplication<Application>().applicationContext)
    val networkState: LiveData<Boolean> = this._networkMonitor.networkState

    init {
        this._networkMonitor.startMonitoringNetwork()
        this._isLoading.value = true
    }

    fun initializeData() {
        if (this._currencies.value?.isNotEmpty() == true)
            return

        viewModelScope.launch {
            this@MainViewModel._currencies.value = this@MainViewModel.fetchAvailableCurrencies(
                this@MainViewModel.getApplication<Application>().applicationContext)
            this@MainViewModel._isLoading.value = false
        }
    }

    private suspend fun fetchAvailableCurrencies(context: Context): List<Currency> = withContext(Dispatchers.IO) {
        val connection = URL(SUPPORTED_CURRENCIES_URL).openConnection()
        connection.connect()
        val stream = connection.getInputStream()
        val result = this@MainViewModel._xmlParser.parseSupportedCurrencies(stream, context)
        stream.close()
        return@withContext result
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

    override fun onCleared() {
        super.onCleared()
        this._networkMonitor.stopMonitoringNetwork()
    }

    companion object {
        private const val SUPPORTED_CURRENCIES_URL = "https://api.currencyfreaks.com/v2.0/supported-currencies?format=xml"
    }
}