package com.example.avocode.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.example.avocode.R
import com.example.avocode.repo.UserImpl
import com.example.avocode.utils.PhoneUtils
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.include_phone_input.*

//Login activity where user can login to the app using phone number and password. Data will be checked from firestore
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        PhoneUtils.setupPhonePrefixesSpinner(phonePrefix)
        PhoneUtils.setupPhoneNumber(editTextPhone)
        editTextPhone.requestFocus()
        buttonNext.setOnClickListener {
            when {
                TextUtils.isEmpty(editTextPhone.text) -> {
                    editTextPhone.error = getString(R.string.message_phone_required)
                    editTextPhone.requestFocus()
                }
                TextUtils.isEmpty(editTextPassword.text) -> {
                    editTextPassword.error = getString(R.string.message_password_required)
                    editTextPassword.requestFocus()
                }
                else -> {
                    PhoneUtils.setLastPhoneNumber(this@LoginActivity, editTextPhone.text.toString())
                    val userImplementation = UserImpl(this@LoginActivity)
                    userImplementation.getLoginUserByPhone("${phonePrefix.selectedItem}${editTextPhone.text.toString().trim { it <= ' ' } }",
                            editTextPassword.text.toString().trim { it <= ' ' })
                }
            }
        }
    }
}
