package com.example.avocode.activity

import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.example.avocode.R
import com.example.avocode.models.FirestoreUserModel
import com.example.avocode.repo.UserImpl
import com.example.avocode.utils.Util
import com.example.avocode.utils.Util.Companion.checkEmptyStrings
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_sign_up3.*
import java.io.File
import java.util.concurrent.TimeUnit

//Main sign up screen where creating firestore data and file upload will done
class SignUpThreeActivity : AppCompatActivity() {

    private var util: Util? = null
    // [START declare_auth]
    private var mAuth: FirebaseAuth? = null
    // [END declare_auth]
    private var mVerificationInProgress = false
    private var mVerificationId: String? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up3)
        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }
        util = Util(this)
        util!!.showLoading(getString(R.string.please_wait))
        val phone = intent.getStringExtra("phone")
        if (!checkEmptyStrings(phone)) {
            editTextPhone.setText(phone)
            buttonSendCode.visibility = View.GONE
        } else {
            buttonSendCode.visibility = View.VISIBLE
        }
        mAuth = FirebaseAuth.getInstance()
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                util!!.showLoading(getString(R.string.message_creating_profile))
                Log.d(TAG, "onVerificationCompleted:$credential")
                mVerificationInProgress = false
                updateUI(STATE_VERIFY_SUCCESS, credential)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                mVerificationInProgress = false
                if (e is FirebaseAuthInvalidCredentialsException) {
                    editTextPhone.error = "Invalid phone number."
                } else if (e is FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.", Snackbar.LENGTH_SHORT).show()
                }
                updateUI(STATE_VERIFY_FAILED)
            }

            override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                Log.d(TAG, "onCodeSent:" + verificationId!!)
                util!!.hideLoading()
                startTimer()
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token
                updateUI(STATE_CODE_SENT)
            }
        }
        viewOTP.requestFocus()
        startPhoneNumberVerification(editTextPhone.text.toString())

        buttonSignUp.setOnClickListener signUpClick@{
            if (!validatePhoneNumber()) {
                return@signUpClick
            }
            if (viewOTP.text == null || checkEmptyStrings(viewOTP.text!!.toString().trim { it <= ' ' })) {
                util!!.toast(getString(R.string.message_add_otp))
            } else {
                val code = viewOTP!!.text!!.toString()
                if (TextUtils.isEmpty(code)) {
                    util!!.toast("OTP Cannot be empty.")
                    return@signUpClick
                } else if (code.length != 6) {
                    util!!.toast("Invalid OTP.")
                    return@signUpClick
                }
                verifyPhoneNumberWithCode(mVerificationId, code)
            }
        }

        buttonSendCode.setOnClickListener {
            if (mVerificationId == null && validatePhoneNumber()) {
                startPhoneNumberVerification(editTextPhone.text.toString())
            } else {
                resendVerificationCode(editTextPhone.text.toString(), mResendToken)
            }
        }
    }

    private fun startTimer() {
        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millSecondsLeftToFinish: Long) {
                val time = (millSecondsLeftToFinish / 1000).toString()
                textView!!.text = getString(R.string.resend_code_text, time)
                textView!!.visibility = View.VISIBLE
                buttonSendCode!!.visibility = View.GONE
            }

            override fun onFinish() {
                textView!!.visibility = View.GONE
                buttonSendCode!!.visibility = View.VISIBLE
                buttonSendCode!!.text = getString(R.string.resend_code)
            }
        }
        timer.start()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this, // Activity (for callback binding)
                mCallbacks!!)        // OnVerificationStateChangedCallbacks

        mVerificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun resendVerificationCode(phoneNumber: String,
                                       token: PhoneAuthProvider.ForceResendingToken?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks!!,
                token)
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val user = task.result!!.user
                        updateUI(STATE_SIGNIN_SUCCESS, user)
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            util!!.toast(getString(R.string.invalid_code))
                            viewOTP!!.setText("")
                        }
                        updateUI(STATE_SIGNIN_FAILED)
                    }
                }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user)
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun updateUI(uiState: Int, user: FirebaseUser? = mAuth!!.currentUser, cred: PhoneAuthCredential? = null) {
        when (uiState) {
            STATE_INITIALIZED -> {
                // Initialized state, show only the phone number field and start button
                enableViews(buttonSendCode!!)
                disableViews(textView!!, linearOTP!!)
            }
            STATE_CODE_SENT -> {
                // Code sent state, show the verification field, the
                enableViews(linearOTP!!)
                disableViews(textView!!)
            }
            STATE_VERIFY_FAILED -> {
                // Verification has failed, show all options
                buttonSendCode!!.text = getString(R.string.resend_code)
                buttonSendCode!!.visibility = View.VISIBLE
                enableViews(buttonSendCode!!, linearOTP!!)
                util!!.hideLoading()
                util!!.toast(getString(R.string.message_verification_failed))
            }
            STATE_VERIFY_SUCCESS -> {
                // Verification has succeeded, proceed to firebase sign in
                disableViews(linearOTP!!, buttonSendCode!!, editTextPhone!!, buttonSignUp!!)
                // Set the verification text based on the credential
                if (cred != null) {
                    Log.d("cred", "cred")
                    if (cred.smsCode != null) {
                        viewOTP!!.setText(cred.smsCode)
                    }
                }
            }
            STATE_SIGNIN_FAILED -> Log.d("STATE_SIGNIN_FAILED", "STATE_SIGNIN_FAILED")
            STATE_SIGNIN_SUCCESS -> Log.d("STATE_SIGNIN_FAILED", "STATE_SIGNIN_FAILED")
        }// No-op, handled by sign-in check
        // Np-op, handled by sign-in check

        if (user != null) {
            uploadFile(Uri.fromFile(File(intent.getStringExtra("uriPath"))), user.phoneNumber)
        }
    }

    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = editTextPhone!!.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            editTextPhone!!.error = getString(R.string.invalid_phone)
            return false
        }

        return true
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    private fun uploadFile(filePath: Uri?, phoneNumber: String?) {
        //if there is a file to upload
        if (filePath != null) {
            val storage = FirebaseStorage.getInstance()
            val storageReference = storage.reference
            //displaying a progress dialog while upload is going on

            val riversRef = storageReference.child("images/" + filePath.toString().substring(filePath.toString().lastIndexOf("/") + 1))
            riversRef.putFile(filePath)
                    .addOnSuccessListener {
                        //if the upload is successful
                        riversRef.downloadUrl.addOnSuccessListener { uri ->
                            //Handle whatever you're going to do with the URL here
                            val firstName = intent.getStringExtra(getString(R.string.firstName))
                            val lastName = intent.getStringExtra(getString(R.string.last_name))
                            val gender = intent.getStringExtra(getString(R.string.gender_label))
                            val dob = intent.getStringExtra(getString(R.string.dob))
                            val password = intent.getStringExtra(getString(R.string.password_label))
                            val uriPath = uri.toString()
                            val userImplementation = UserImpl(this@SignUpThreeActivity)
                            val firestoreUserModel = FirestoreUserModel()
                            firestoreUserModel.firstName = firstName
                            firestoreUserModel.lastName = lastName
                            firestoreUserModel.dob = dob
                            firestoreUserModel.gender = gender
                            firestoreUserModel.password = password
                            firestoreUserModel.phone = phoneNumber
                            firestoreUserModel.uriPath = uriPath
                            firestoreUserModel.familyCode = Util.generateFamilyCode()
                            util!!.hideLoading()
                            userImplementation.doesUserExist(phoneNumber!!, firestoreUserModel)
                        }
                    }
                    .addOnFailureListener { exception ->
                        //if the upload is not successful
                        util!!.hideLoading()
                        util!!.toast(exception.message)
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        util!!.log("Progress", "" + progress)
                    }
        } else {
            //you can display an error toast
            util!!.toast(getString(R.string.message_picture_empty))
        }//if there is not any file
    }

    companion object {
        private val TAG = SignUpThreeActivity::class.java.simpleName
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_CODE_SENT = 2
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }


}
