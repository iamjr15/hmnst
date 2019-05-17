package com.example.avocode.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.avocode.R
import com.orm.SugarRecord.findById
import dbmodel.User
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val user = findById(User::class.java, 1)
        if (user != null) {
            textViewPhone.text = user.phone
            textViewFamilyCode.text = user.id.toString()
        }

    }
}