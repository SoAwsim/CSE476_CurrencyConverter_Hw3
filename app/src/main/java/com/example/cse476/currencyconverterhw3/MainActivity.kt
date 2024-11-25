package com.example.cse476.currencyconverterhw3

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cse476.currencyconverterhw3.extensions.toUIString
import com.example.cse476.currencyconverterhw3.models.spinner.CustomSpinnerAdapter
import com.example.cse476.currencyconverterhw3.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private val model: MainViewModel by viewModels()

    private lateinit var progressBarStatusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var editTextFrom: EditText
    private lateinit var editTextTo: EditText
    private lateinit var convertButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.progressBarStatusText = this.findViewById(R.id.progressStatus)
        this.progressBar = this.findViewById(R.id.progressBar)
        this.textView= this.findViewById(R.id.textView)
        this.spinnerFrom = this.findViewById(R.id.currencyConvertFrom)
        this.spinnerTo = this.findViewById(R.id.currencyConvertTo)
        this.editTextFrom = this.findViewById(R.id.currencyConvertFromValue)
        this.editTextTo = this.findViewById(R.id.currencyConvertToValue)
        this.convertButton = this.findViewById(R.id.button)

        this.setupViewModelObservers()
        this.setupListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun setupViewModelObservers() {
        // I know this looks disgusting but I did not want to create another activity
        model.isLoading.observe(this) { isLoading ->
            val loadingComponentVisibility = if (isLoading) View.VISIBLE else View.GONE
            val coreComponentVisibility = if (isLoading) View.GONE else View.VISIBLE

            this.progressBarStatusText.visibility = loadingComponentVisibility
            this.progressBar.visibility = loadingComponentVisibility

            this.textView.visibility = coreComponentVisibility
            this.spinnerFrom.visibility = coreComponentVisibility
            this.spinnerTo.visibility = coreComponentVisibility
            this.editTextFrom.visibility = coreComponentVisibility
            this.editTextTo.visibility = coreComponentVisibility
            this.convertButton.visibility = coreComponentVisibility
        }

        model.networkState.observe(this) { connected ->
            if (connected) {
                progressBarStatusText.text = "Connected fetching data from API"
                this.model.initializeData()
            } else if(this.model.currencies.value?.isEmpty() == true) {
                progressBarStatusText.text = "Waiting for connection"
                Toast.makeText(
                    this,
                    "Internet connection not available",
                    Toast.LENGTH_LONG).show()
            }
        }

        model.currencies.observe(this) { currencies ->
            if (currencies.isNotEmpty()) {
                val adapter = CustomSpinnerAdapter(
                    this,
                    currencies
                )
                spinnerFrom.adapter = adapter
                spinnerTo.adapter = adapter
            }
        }

        model.fromCurrencyValue.observe(this) { value ->
            // Prevent unnecessary event firing
            if (editTextFrom.text.toString().toDoubleOrNull() != value)
                editTextFrom.setText(value.toUIString())
        }

        model.toCurrencyValue.observe(this) { value ->
            if (editTextTo.text.toString().toDoubleOrNull() != value)
                editTextTo.setText(value.toUIString())
        }

        model.convertOperationRunning.observe(this) { status ->
            this.convertButton.setEnabled(!status)
            this.spinnerFrom.setEnabled(!status)
            this.spinnerTo.setEnabled(!status)
        }
    }

    private fun setupListeners() {
        this.editTextFrom.addTextChangedListener(object : TextWatcher {
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
                if (numericValue == null) {
                    Toast.makeText(
                        this@MainActivity,
                        "Please enter a valid number",
                        Toast.LENGTH_SHORT).show()
                }
            }
        })

        this.spinnerFrom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                this@MainActivity.model.clearToCurrency()
                this@MainActivity.model.currencyFromIndex = position
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                this@MainActivity.model.clearToCurrency()
                this@MainActivity.model.currencyFromIndex = -1
            }
        }

        this.spinnerTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                this@MainActivity.model.currencyToIndex = position
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                this@MainActivity.model.currencyToIndex = -1
            }

        }

        this.convertButton.setOnClickListener {
            if (this.model.networkState.value != true) {
                Toast.makeText(
                    this,
                    "Network disconnected, cannot convert!",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            this.model.convertButton()
        }
    }
}