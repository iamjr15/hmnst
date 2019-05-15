package com.example.avocode.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Context.TELEPHONY_SERVICE
import android.telephony.TelephonyManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.example.avocode.R


object PhoneUtils {

    private const val PHONE_PREFS = "PhonePrefs"
    private const val PHONE_PREFIX = "PhonePrefix"
    private const val PHONE_NUMBER = "PhoneNumber"

    fun setupPhonePrefixesSpinner(spinner: Spinner) {
        val ctx = spinner.context

        val phonePrefixes = ctx.resources.getStringArray(R.array.phonePrefixes).sorted().distinct()
        spinner.adapter = ArrayAdapter<String>(ctx, R.layout.phone_prefix_spinner_item, phonePrefixes)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setLastPhonePrefix(ctx, phonePrefixes[position])
            }
        }
        spinner.setSelection(phonePrefixes.indexOf(getLastPhonePrefix(ctx) ?: "+" + getCountryZipCode(ctx)))
    }

    fun setupPhoneNumber(phoneNumber: EditText) {
        val ctx = phoneNumber.context
        phoneNumber.setText(getLastPhoneNumber(ctx))
    }

    fun setLastPhoneNumber(context: Context, phoneNumber: String) {
        val editor = context.getSharedPreferences(PHONE_PREFS, MODE_PRIVATE).edit()
        editor.putString(PHONE_NUMBER, phoneNumber).apply()
    }

    fun getLastPhoneNumber(context: Context): String? {
        val prefs = context.getSharedPreferences(PHONE_PREFS, MODE_PRIVATE)
        return prefs.getString(PHONE_NUMBER, null)
    }

    fun setLastPhonePrefix(context: Context, phonePrefix: String) {
        val editor = context.getSharedPreferences(PHONE_PREFS, MODE_PRIVATE).edit()
        editor.putString(PHONE_PREFIX, phonePrefix).apply()
    }

    fun getLastPhonePrefix(context: Context): String? {
        val prefs = context.getSharedPreferences(PHONE_PREFS, MODE_PRIVATE)
        return prefs.getString(PHONE_PREFIX, null)
    }

    fun getCountryZipCode(context: Context): String {
        var countryZipCode = "1" // US by default

        val manager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        //getNetworkCountryIso
        val countryId = manager.simCountryIso.toUpperCase()
        val countryCodes = context.resources.getStringArray(R.array.countryCodes)
        for (code in countryCodes) {
            val id = code.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (id[1].trim { it <= ' ' } == countryId.trim { it <= ' ' }) {
                countryZipCode = id[0]
                break
            }
        }
        return countryZipCode
    }

    fun getFullPhoneNumber(phonePrefix: Spinner, phoneNumber: EditText) =
            "${phonePrefix.selectedItem}${phoneNumber.text.toString().trim { it <= ' ' } }"
}