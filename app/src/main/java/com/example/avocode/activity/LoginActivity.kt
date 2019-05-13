package com.example.avocode.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.example.avocode.R
import com.example.avocode.repo.UserImpl
import kotlinx.android.synthetic.main.activity_login.*

//Login activity where user can login to the app using phone number and password. Data will be checked from firestore
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        buttonNext.setOnClickListener {
            when {
                TextUtils.isEmpty(editTextPhone!!.text.toString()) -> {
                    editTextPhone!!.error = getString(R.string.message_phone_required)
                    editTextPhone!!.requestFocus()
                }
                TextUtils.isEmpty(editTextPassword!!.text.toString()) -> {
                    editTextPassword!!.error = getString(R.string.message_password_required)
                    editTextPassword!!.requestFocus()
                }
                else -> {
                    val userImplementation = UserImpl(this@LoginActivity)
                    userImplementation.getLoginUserByPhone("+91" + editTextPhone!!.text.toString().trim { it <= ' ' }, editTextPassword!!.text.toString().trim { it <= ' ' })
                }
            }
        }
    }
}
