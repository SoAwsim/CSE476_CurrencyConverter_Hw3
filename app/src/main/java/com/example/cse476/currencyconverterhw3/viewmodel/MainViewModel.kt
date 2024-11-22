package com.example.cse476.currencyconverterhw3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class CurrencyFieldState(
    val currencyFromNumber: Double? = null,
    val currencyToNumber: Double? = null
)

class MainViewModel: ViewModel() {
    private val _currencyFieldState = MutableLiveData(CurrencyFieldState())
    val currencyFieldState: LiveData<CurrencyFieldState> = _currencyFieldState

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
}