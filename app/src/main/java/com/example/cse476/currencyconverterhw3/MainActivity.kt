package com.example.cse476.currencyconverterhw3

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cse476.currencyconverterhw3.databinding.ActivityMainBinding
import com.example.cse476.currencyconverterhw3.extensions.toUIString
import com.example.cse476.currencyconverterhw3.models.spinner.CustomSpinnerAdapter
import com.example.cse476.currencyconverterhw3.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private val model: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private var currentToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        this.binding = ActivityMainBinding.inflate(this.layoutInflater)
        setContentView(this.binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(this.binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.setupViewModelObservers()
        this.setupListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViewModelObservers() {
        // I know this looks disgusting but I did not want to create another activity
        model.isLoading.observe(this) { isLoading ->
            val loadingComponentVisibility = if (isLoading) View.VISIBLE else View.GONE
            val coreComponentVisibility = if (isLoading) View.GONE else View.VISIBLE

            this.binding.progressStatus.visibility = loadingComponentVisibility
            this.binding.progressBar.visibility = loadingComponentVisibility

            this.binding.infoTextView.visibility = coreComponentVisibility
            this.binding.currencyFromTextView.visibility = coreComponentVisibility
            this.binding.currencyConvertFromSpinner.visibility = coreComponentVisibility
            this.binding.currencyToTextView.visibility = coreComponentVisibility
            this.binding.currencyConvertToSpinner.visibility = coreComponentVisibility
            this.binding.fromValueTextView.visibility = coreComponentVisibility
            this.binding.currencyConvertFromEditText.visibility = coreComponentVisibility
            this.binding.resultTextView.visibility = coreComponentVisibility
            this.binding.currencyConvertToEditText.visibility = coreComponentVisibility
            this.binding.convertButton.visibility = coreComponentVisibility
        }

       this.model.networkState.observe(this) { connected ->
            if (connected) {
                this.binding.progressStatus.text = "Connected fetching data from API"
                this.model.initializeData()
            } else if(this.model.currencies.value?.isEmpty() == true) {
                this.binding.progressStatus.text = "Waiting for connection"
            }
        }

        this.model.currencies.observe(this) { currencies ->
            if (currencies?.isNotEmpty() == true) {
                val adapter = CustomSpinnerAdapter(
                    this,
                    currencies
                )
                this.binding.currencyConvertFromSpinner.adapter = adapter
                this.binding.currencyConvertToSpinner.adapter = adapter
            }
        }

        this.model.fromCurrencyValue.observe(this) { value ->
            if (this.binding.currencyConvertFromEditText.text.toString().toDoubleOrNull() != value)
                this.binding.currencyConvertFromEditText.setText(value.toUIString())
        }

        this.model.toCurrencyValue.observe(this) { value ->
            if (this.binding.currencyConvertToEditText.text.toString().toDoubleOrNull() != value)
                this.binding.currencyConvertToEditText.setText(value.toUIString())
        }

        this.model.convertOperationRunning.observe(this) { status ->
            this.binding.convertButton.setEnabled(!status)
            this.binding.currencyConvertFromSpinner.setEnabled(!status)
            this.binding.currencyConvertToSpinner.setEnabled(!status)
        }

        this.model.errorMessage.observe(this) { message ->
            this.showToastMessage(message)
        }
    }

    private fun setupListeners() {
        this.binding.currencyConvertFromEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                val input = (s ?: "").toString()
                if (input.isEmpty()) {
                    this@MainActivity.model.updateFromCurrency(null)
                    return
                }

                val numericValue = input.toDoubleOrNull()
                this@MainActivity.model.updateFromCurrency(numericValue)
                if (numericValue == null)
                    this@MainActivity.showToastMessage("Please enter a valid number")
            }
        })

        this.binding.currencyConvertFromSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (this@MainActivity.model.currencyFromIndex == position)
                        return

                    this@MainActivity.model.clearToCurrency()
                    this@MainActivity.model.currencyFromIndex = position
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    this@MainActivity.model.clearToCurrency()
                    this@MainActivity.model.currencyFromIndex = -1
                }
            }

        this.binding.currencyConvertToSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (this@MainActivity.model.currencyToIndex == position)
                        return

                    this@MainActivity.model.clearToCurrency()
                    this@MainActivity.model.currencyToIndex = position
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    this@MainActivity.model.clearToCurrency()
                    this@MainActivity.model.currencyToIndex = -1
                }
            }

        this.binding.convertButton.setOnClickListener {
            this.model.convertButton()
        }
    }

    private fun showToastMessage(message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }
}