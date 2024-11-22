package com.example.cse476.currencyconverterhw3

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
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

        val editTextFrom = this.findViewById<EditText>(R.id.currencyConvertFromValue)
        val editTextTo = this.findViewById<EditText>(R.id.currencyConvertToValue)

        model.currencyFieldState.observe(this) { state ->
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

        val convertButton = this.findViewById<Button>(R.id.button)

        convertButton.setOnClickListener {
            model.convertButton()
        }
    }
}