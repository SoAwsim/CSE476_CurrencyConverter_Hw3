package com.example.cse476.currencyconverterhw3

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
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
import com.example.cse476.currencyconverterhw3.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val progressBar = this.findViewById<ProgressBar>(R.id.progressBar)
        val textView = this.findViewById<TextView>(R.id.textView)
        val spinnerFrom = this.findViewById<Spinner>(R.id.currencyConvertFrom)
        val spinnerTo = this.findViewById<Spinner>(R.id.currencyConvertTo)
        val editTextFrom = this.findViewById<EditText>(R.id.currencyConvertFromValue)
        val editTextTo = this.findViewById<EditText>(R.id.currencyConvertToValue)
        val convertButton = this.findViewById<Button>(R.id.button)

        // I know this looks disgusting but I did not want to create another activity
        model.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            val coreComponentVisibility = if (isLoading) View.GONE else View.VISIBLE
            textView.visibility = coreComponentVisibility
            spinnerFrom.visibility = coreComponentVisibility
            spinnerTo.visibility = coreComponentVisibility
            editTextFrom.visibility = coreComponentVisibility
            editTextTo.visibility = coreComponentVisibility
            convertButton.visibility = coreComponentVisibility
        }

        model.currencies.observe(this) { currencies ->
            if (currencies.isNotEmpty()) {
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    currencies
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerFrom.adapter = adapter
                spinnerTo.adapter = adapter
            }
        }

        model.currencyField.observe(this) { state ->
            val fromValue = state.currencyFromNumber

            // Prevent unnecessary event firing
            if (editTextFrom.text.toString().toDoubleOrNull() != fromValue)
                editTextFrom.setText(fromValue.toUIString())

            editTextTo.setText(state.currencyToNumber.toUIString())
        }

        editTextFrom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                val input = (s ?: "").toString()
                if (input.isEmpty()) {
                    model.updateFromCurrency(null)
                    return
                }

                val numericValue = input.toDoubleOrNull()
                model.updateFromCurrency(numericValue)
                if (numericValue == null) {
                    Toast.makeText(
                        this@MainActivity,
                        "Please enter a valid number",
                        Toast.LENGTH_SHORT).show()
                }
            }
        })

        convertButton.setOnClickListener {
            model.convertButton()
        }
    }
}