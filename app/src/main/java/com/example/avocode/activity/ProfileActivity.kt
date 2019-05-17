package com.example.avocode.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.avocode.R
import com.example.avocode.utils.Util
import com.orm.SugarRecord.findById
import dbmodel.User
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val user = findById(User::class.java, 1)
        if (user != null) {
            Util.showFamilyCode(textViewFamilyCode, user.familyCode)
            textViewFullName.text = String.format(getString(R.string.fullName), user.firstName, user.lastName)
            editTextDOB.text = user.dob
            editTextFirstName.setText(user.firstName)
            editTextLastName.setText(user.lastName)
            if (!Util.checkEmptyStrings(user.uriPath)) {
                val avatar = Glide.with(this).load(user.uriPath)
                avatar.into(imageViewProfilePic)
            }
            editTextGender.text = user.gender
            flPhone.setText(user.phone)
        }
    }
}