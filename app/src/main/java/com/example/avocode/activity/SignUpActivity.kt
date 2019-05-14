package com.example.avocode.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.avocode.AvaApplication
import com.example.avocode.R
import com.example.avocode.config.Constants
import com.example.avocode.utils.PhoneUtils
import com.example.avocode.utils.Util
import com.example.avocode.utils.Util.Companion.checkEmptyStrings
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.include_phone_input.*
import java.text.SimpleDateFormat
import java.util.*

//First sign up screen to take user's basic info
class SignUpActivity : AppCompatActivity() {

    private var util: Util? = null
    private val myCalendar = Calendar.getInstance()
    private lateinit var date: DatePickerDialog.OnDateSetListener

    private lateinit var gender: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        util = Util(this)

        date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateText()
        }

        editTextDOB.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this@SignUpActivity, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        PhoneUtils.setupPhonePrefixesSpinner(phonePrefix)
        buttonSignUp.setOnClickListener {
            when {
                checkEmptyStrings(editTextFirstName.text.toString()) -> {
                    editTextFirstName.error = getString(R.string.message_first_name_required)
                    editTextFirstName.requestFocus()
                }
                checkEmptyStrings(editTextLastName.text.toString()) -> {
                    editTextLastName.error = getString(R.string.message_last_name_required)
                    editTextLastName.requestFocus()
                }
                checkEmptyStrings(editTextPassword.text.toString()) -> {
                    editTextPassword.error = getString(R.string.message_password_required)
                    editTextPassword.requestFocus()
                }
                checkEmptyStrings(editTextPhone.text.toString()) -> {
                    editTextPhone.error = getString(R.string.message_phone_required)
                    editTextPhone.requestFocus()
                }
                else -> {
                    util!!.showLoading(getString(R.string.please_wait))
                    val db = AvaApplication.instance.dbInstance
                    val docSnapshot = db!!.collection(Constants.USER_COLLECTION).document(
                            PhoneUtils.getFullPhoneNumber(phonePrefix, editTextPhone)
                    ).get()
                    docSnapshot.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document != null && document.exists()) {
                                // user already exist in database
                                util!!.toast(getString(R.string.message_user_already_exist))
                            } else {
                                //user does not exist in database
                                addNewRegisteredUser()
                            }
                            util!!.hideLoading()
                        } else {
                            //user does not exist in database
                            addNewRegisteredUser()
                        }
                    }
                }
            }
        }
    }

    private fun updateText() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        editTextDOB.text = sdf.format(myCalendar.time)
    }

    private fun addNewRegisteredUser() {
        util!!.hideLoading()
        gender = if (rbMale!!.isChecked)
            getString(R.string.male)
        else
            getString(R.string.female)

        val intent = Intent(this@SignUpActivity, SignUpTwoActivity::class.java)
        intent.putExtra(getString(R.string.firstName), editTextFirstName!!.text.toString().trim { it <= ' ' })
        intent.putExtra(getString(R.string.last_name), editTextLastName!!.text.toString().trim { it <= ' ' })
        intent.putExtra(getString(R.string.gender_label), gender)
        intent.putExtra(getString(R.string.dob), editTextDOB.text.toString().trim { it <= ' ' })
        intent.putExtra(getString(R.string.password_label), editTextPassword!!.text.toString().trim { it <= ' ' })
        intent.putExtra(getString(R.string.phone_label), PhoneUtils.getFullPhoneNumber(phonePrefix, editTextPhone))
        startActivity(intent)
    }

}
