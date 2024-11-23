package com.example.cse476.currencyconverterhw3.models.spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.cse476.currencyconverterhw3.R
import com.example.cse476.currencyconverterhw3.models.currency.Currency

class CustomSpinnerAdapter(
    context: Context,
    items: List<Currency>
) : ArrayAdapter<Currency>(context, 0, items) {
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        return this.createViewFromResource(position, convertView, parent)
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        return this.createViewFromResource(position, convertView, parent)
    }

    private fun createViewFromResource(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view = convertView ?: LayoutInflater.from(this.context).inflate(
            R.layout.custom_spinner_item,
            parent,
            false
        )
        val item = this.getItem(position)

        val currencyImageView = view.findViewById<ImageView>(R.id.currencyImage)
        val currencyTextView = view.findViewById<TextView>(R.id.currencyCode)

        currencyImageView.setImageBitmap(item?.icon)
        currencyTextView.text = item?.currencyCode

        return view
    }
}