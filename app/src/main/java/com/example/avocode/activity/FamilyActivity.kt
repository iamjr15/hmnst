package com.example.avocode.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.avocode.R
import kotlinx.android.synthetic.main.activity_family.*

class FamilyActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family)
        textViewFamilyCode.text = String.format(getString(R.string.family_code),
                intent.getStringExtra(getString(R.string.familyCode)))
    }
}