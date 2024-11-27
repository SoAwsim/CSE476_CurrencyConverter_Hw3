package com.example.cse476.currencyconverterhw3.viewmodel

import SingleLiveEvent
import android.app.Application
import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.URL

class MainViewModel(application: Application): AndroidViewModel(application) {
    private val _fromCurrencyValue = MutableLiveData<Double?>(null)
    val fromCurrencyValue: LiveData<Double?> = this._fromCurrencyValue

    private val _toCurrencyValue = MutableLiveData<Double?>(null)
    val toCurrencyValue: LiveData<Double?> = this._toCurrencyValue

    private val _convertOperationRunning = MutableLiveData(false)
    val convertOperationRunning: LiveData<Boolean> = this._convertOperationRunning

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = this._isLoading

    private val _currencies = MutableLiveData<List<Currency>?>()
    val currencies: LiveData<List<Currency>?> = this._currencies

    private val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String> = this._errorMessage

    private val _networkMonitor =
        NetworkMonitor(this.getApplication<Application>().applicationContext)
    val networkState: LiveData<Boolean> = this._networkMonitor.networkState

    private val _supportedCurrencySemaphore = Semaphore(1)
    private val _supportedCurrencyParser = SupportedCurrenciesXmlParser()

    private val _usdConversionRateParser = UsdConversionRateParser()

    var currencyFromIndex = 0
    var currencyToIndex = 0

    init {
        this._networkMonitor.startMonitoringNetwork()
    }

    override fun onCleared() {
        super.onCleared()
        this._networkMonitor.stopMonitoringNetwork()
    }

    fun initializeData() {
        if (this._currencies.value?.isNotEmpty() == true)
            return

        viewModelScope.launch {
            this@MainViewModel._supportedCurrencySemaphore.withPermit {
                if (this@MainViewModel._currencies.value?.isNotEmpty() == true)
                    return@withPermit

                if (this@MainViewModel.networkState.value != true)
                    return@withPermit

                this@MainViewModel._currencies.value = this@MainViewModel.fetchResponseWithRetry {
                    return@fetchResponseWithRetry this@MainViewModel.fetchAvailableCurrencies(
                        this@MainViewModel.getApplication<Application>().applicationContext)
                }

                if (this@MainViewModel._currencies.value == null)
                    return@withPermit

                this@MainViewModel._isLoading.value = false
            }
        }
    }

    fun updateFromCurrency(value: Double?) {
        this._fromCurrencyValue.value = value
        this.clearToCurrency()
    }

    fun clearToCurrency() {
        this._toCurrencyValue.value = null
    }

    fun convertButton() {
        this._convertOperationRunning.value = true
        viewModelScope.launch {
            try {
                val fromValue = this@MainViewModel._fromCurrencyValue.value ?: return@launch

                // If no currency is selected directly return
                if (this@MainViewModel.currencyFromIndex < 0 || this@MainViewModel.currencyToIndex < 0)
                    return@launch

                // If the selected currencies are the same do not process just return the same number
                if (this@MainViewModel.currencyToIndex == this@MainViewModel.currencyFromIndex) {
                    this@MainViewModel._toCurrencyValue.value = fromValue
                    return@launch
                }

                val selectedFrom = this@MainViewModel._currencies.value?.get(currencyFromIndex)
                    ?: return@launch
                val selectedTo = this@MainViewModel._currencies.value?.get(currencyToIndex)
                    ?: return@launch

                // Check if network is available one last time before sending the request
                if (this@MainViewModel.networkState.value != true) {
                    this@MainViewModel._errorMessage.value =
                        "Network disconnected cannot convert!"
                    return@launch
                }

                val conversionMap = try {
                    this@MainViewModel.fetchUsdConversionRates()
                } catch (e: Exception) {
                    this@MainViewModel._errorMessage.value =
                        "Fetching conversion rates from the API failed!"
                    Log.e(
                        TAG,
                        "Fetching conversion rates failed with the following exception",
                        e)
                    return@launch
                }
                val result = selectedFrom.convertToCurrency(
                    fromValue,
                    selectedTo.currencyCode,
                    conversionMap
                )
                this@MainViewModel._toCurrencyValue.value = result
            } finally {
                this@MainViewModel._convertOperationRunning.value = false
            }
        }
    }

    private suspend fun fetchAvailableCurrencies(
        context: Context
    ): List<Currency> = withContext(Dispatchers.IO) {
        val connection = URL(SUPPORTED_CURRENCIES_URL).openConnection()
        connection.connect()
        val stream = connection.getInputStream()
        val result = this@MainViewModel._supportedCurrencyParser.parseSupportedCurrencies(stream, context)

        try {
            stream.close()
        } catch (e: Exception) {
            Log.w(TAG,
                "Closing connection for available currencies failed after getting the result.",
                e)
        }

        return@withContext result
    }

    private suspend fun fetchUsdConversionRates()
    : Map<String, Double> = withContext(Dispatchers.IO) {
        val connection = URL(CURRENCY_API).openConnection()
        connection.connect()
        val stream = connection.getInputStream()
        val result = _usdConversionRateParser.parseUsdConversionRates(stream)

        try {
            stream.close()
        } catch (e: Exception) {
            Log.w(TAG,
                "Closing connection for conversion rate failed after getting the result.",
                e)
        }

        return@withContext result
    }

    private suspend fun <T> fetchResponseWithRetry(
        apiAction: suspend () -> T,
    ): T? {
        for (retry in 1..4) {
            try {
                if (this@MainViewModel.networkState.value != true)
                    return null

                return apiAction()
            } catch (e: Exception) {
                if (retry != 4) {
                    Log.w(
                        TAG,
                        "Api fetch failed! Retrying again in 5 seconds. " +
                                "Attempt $retry/4",
                        e)
                    delay(5000)
                    continue
                }
                val errorMessage = "Api fetch retry limit of 4 reached. " +
                        "Waiting for another network event to retry"
                this._errorMessage.value = errorMessage
                Log.e(TAG, errorMessage, e)
            }
        }
        return null
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val SUPPORTED_CURRENCIES_URL =
            "https://api.currencyfreaks.com/v2.0/supported-currencies?format=xml"
        private const val CURRENCY_API =
            "https://api.currencyfreaks.com/v2.0/rates/latest?apikey=" + BuildConfig.API_KEY + "&format=xml"
    }
}