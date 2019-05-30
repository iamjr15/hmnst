package com.example.avocode.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.avocode.R
import com.example.avocode.config.Constants
import com.example.avocode.repo.UserImpl
import com.example.avocode.utils.Util
import com.orm.SugarRecord.findById
import dbmodel.User
import kotlinx.android.synthetic.main.activity_profile.*
import java.text.SimpleDateFormat
import java.util.*


class ProfileActivity: AppCompatActivity() {

    private val myCalendar = Calendar.getInstance()
    private val userImpl = UserImpl(this)
    private val user = findById(User::class.java, 1)!!
    private lateinit var date: DatePickerDialog.OnDateSetListener

    private val drawableTouchListener = View.OnTouchListener { v, event ->
        val DRAWABLE_LEFT = 0
        val DRAWABLE_TOP = 1
        val DRAWABLE_RIGHT = 2
        val DRAWABLE_BOTTOM = 3

        if(event.action == MotionEvent.ACTION_UP && v is TextView &&
                event.rawX >= (v.right - v.compoundDrawables[DRAWABLE_RIGHT].bounds.width() * 2)) {
            if(v.id == R.id.editTextDOB){
                val datePickerDialog = DatePickerDialog(this@ProfileActivity, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH))
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                datePickerDialog.show()
            } else if (v.id == R.id.editTextGender) {
                if (editTextGender.text.toString() == getString(R.string.female)) {
                    editTextGender.setText(R.string.male)
                } else {
                    editTextGender.setText(R.string.female)
                }
                updateFirestore(v)
            } else {
                val dialogA = object {
                    fun showDialog(activity: Activity, msg: String, curr: String) {
                        val dialog = Dialog(activity)
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setCancelable(false)
                        dialog.setContentView(R.layout.dialog_profile)

                        val text = dialog.findViewById(R.id.textViewHeader) as TextView
                        text.text = msg
                        val textViewCancel = dialog.findViewById(R.id.textViewCancel) as TextView
                        val textViewOkay = dialog.findViewById(R.id.textViewOk) as TextView
                        val editText = dialog.findViewById(R.id.editTextVal) as TextView
                        editText.hint = msg
                        editText.text = curr
                        textViewCancel.setOnClickListener { dialog.dismiss() }
                        textViewOkay.setOnClickListener {
                            v.text = editText.text.toString()
                            updateFirestore(v)
                            dialog.dismiss()
                        }
                        dialog.show()
                    }
                }
                if(v == editTextFirstName){
                    dialogA.showDialog(this@ProfileActivity, getString(R.string.first_name), v.text.toString())
                } else if (v == editTextLastName) {
                    dialogA.showDialog(this@ProfileActivity, getString(R.string.last_name), v.text.toString())
                }
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        Util.showFamilyCode(textViewFamilyCode, user.familyCode)
        textViewFullName.text = String.format(getString(R.string.fullName), user.firstName, user.lastName)
        editTextDOB.setText(user.dob)
        editTextFirstName.setText(user.firstName)
        editTextLastName.setText(user.lastName)
        if (!Util.checkEmptyStrings(user.uriPath)) {
            val avatar = Glide.with(this).load(user.uriPath)
            avatar.into(imageViewProfilePic)
        }
        editTextGender.setText(user.gender)
        flPhone.setText(user.phone)

        editTextFirstName.setOnTouchListener(drawableTouchListener)
        editTextLastName.setOnTouchListener(drawableTouchListener)
        editTextDOB.setOnTouchListener(drawableTouchListener)
        editTextGender.setOnTouchListener(drawableTouchListener)

        date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDOB()
            user.dob = editTextDOB.text.toString()
            user.save()
        }
    }

    fun updateFirestore(v: View) {
        val phone = user.phone
        when(v) {
            editTextFirstName -> {
                userImpl.updateUserField(phone, Constants.DocumentFields.FIRST_NAME, editTextFirstName.text.toString())
                user.firstName = editTextFirstName.text.toString()
                textViewFullName.text = String.format(getString(R.string.fullName), user.firstName, user.lastName)
            }
            editTextLastName -> {
                userImpl.updateUserField(phone, Constants.DocumentFields.LAST_NAME, editTextLastName.text.toString())
                user.lastName = editTextLastName.text.toString()
                textViewFullName.text = String.format(getString(R.string.fullName), user.firstName, user.lastName)
            }
            editTextDOB -> {
                userImpl.updateUserField(phone, Constants.DocumentFields.DOB, editTextDOB.text.toString())
                user.dob = editTextDOB.text.toString()
            }
            editTextGender -> {
                userImpl.updateUserField(phone, Constants.DocumentFields.GENDER, editTextGender.text.toString())
                user.gender = editTextGender.text.toString()
            }
            else -> return
        }
        user.save()
    }

    fun showKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    private fun updateDOB() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        editTextDOB.setText(sdf.format(myCalendar.time))
        userImpl.updateUserField(flPhone.text.toString(), Constants.DocumentFields.DOB, editTextDOB.text.toString())
    }
}