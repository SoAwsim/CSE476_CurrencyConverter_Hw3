package com.example.cse476.currencyconverterhw3.models.spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.cse476.currencyconverterhw3.databinding.CustomSpinnerItemBinding
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
        val binding = if (convertView == null) {
            CustomSpinnerItemBinding.inflate(
                LayoutInflater.from(this.context),
                parent,
                false)
        } else {
            CustomSpinnerItemBinding.bind(convertView)
        }
        val item = this.getItem(position)

        binding.currencyImage.setImageBitmap(item?.icon)
        binding.currencyCode.text = item?.currencyCode

        return binding.root
    }
}