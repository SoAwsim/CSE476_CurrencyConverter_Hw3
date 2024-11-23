package com.example.cse476.currencyconverterhw3.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.cse476.currencyconverterhw3.BuildConfig
import com.example.cse476.currencyconverterhw3.models.currency.Currency
import com.example.cse476.currencyconverterhw3.models.network.NetworkMonitor
import com.example.cse476.currencyconverterhw3.xml.SupportedCurrenciesXmlParser
import com.example.cse476.currencyconverterhw3.xml.UsdConversionRateParser
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

    private val _networkMonitor =
        NetworkMonitor(this.getApplication<Application>().applicationContext)
    val networkState: LiveData<Boolean> = this._networkMonitor.networkState

    private val _supportedCurrencyParser = SupportedCurrenciesXmlParser()
    private val _usdConversionRateParser = UsdConversionRateParser()

    var currencyFromIndex = 0
    var currencyToIndex = 0

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

    private suspend fun fetchAvailableCurrencies(
        context: Context
    ): List<Currency> = withContext(Dispatchers.IO) {
        val connection = URL(SUPPORTED_CURRENCIES_URL).openConnection()
        connection.connect()
        val stream = connection.getInputStream()
        val result = this@MainViewModel._supportedCurrencyParser.parseSupportedCurrencies(stream, context)
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
        viewModelScope.launch {
            val fromNumber = this@MainViewModel._currencyField.value?.currencyFromNumber
                ?: return@launch

            // If no currency is selected directly return
            if (this@MainViewModel.currencyFromIndex < 0 || this@MainViewModel.currencyToIndex < 0)
                return@launch

            // If the selected currencies are the same do not process just return the same number
            if (this@MainViewModel.currencyToIndex == this@MainViewModel.currencyFromIndex) {
                _currencyField.value = CurrencyFieldState(
                    currencyFromNumber = fromNumber,
                    currencyToNumber = fromNumber
                )
                return@launch
            }

            val selectedFrom = this@MainViewModel._currencies.value?.get(currencyFromIndex)
                ?: return@launch
            val selectedTo = this@MainViewModel._currencies.value?.get(currencyToIndex)
                ?: return@launch

            val conversionMap = this@MainViewModel.fetchUsdConversionRates()
            val result = selectedFrom.convertToCurrency(
                fromNumber,
                selectedTo.currencyCode,
                conversionMap
            )

            _currencyField.value = CurrencyFieldState(
                currencyFromNumber = fromNumber,
                currencyToNumber = result
            )
        }
    }

    private suspend fun fetchUsdConversionRates()
    : Map<String, Double> = withContext(Dispatchers.IO) {
        val connection = URL(CURRENCY_API).openConnection()
        connection.connect()
        val stream = connection.getInputStream()
        val result = _usdConversionRateParser.parseUsdConversionRates(stream)
        stream.close()
        return@withContext result
    }

    override fun onCleared() {
        super.onCleared()
        this._networkMonitor.stopMonitoringNetwork()
    }

    companion object {
        private const val SUPPORTED_CURRENCIES_URL =
            "https://api.currencyfreaks.com/v2.0/supported-currencies?format=xml"
        private const val CURRENCY_API =
            "https://api.currencyfreaks.com/v2.0/rates/latest?apikey=" + BuildConfig.API_KEY + "&format=xml"
    }
}