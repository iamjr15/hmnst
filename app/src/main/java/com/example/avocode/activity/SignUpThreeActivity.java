package com.example.avocode.activity;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.avocode.R;
import com.example.avocode.models.FirestoreUserModel;
import com.example.avocode.repo.UserImpl;
import com.example.avocode.utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mukesh.OtpView;

import java.io.File;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.avocode.utils.Util.checkEmptyStrings;

//Main sign up screen where creating firestore data and file upload will done
public class SignUpThreeActivity extends AppCompatActivity {

    private static final String TAG = SignUpThreeActivity.class.getSimpleName();

    @BindView(R.id.editTextPhone)
    EditText editTextPhone;
    @BindView(R.id.textView)
    TextView textView;

    @BindView(R.id.buttonSendCode)
    Button buttonSendCode;

    @BindView(R.id.buttonSignUp)
    Button buttonSignUp;

    @BindView(R.id.linearOTP)
    LinearLayout linearOTP;

    @BindView(R.id.viewOTP)
    OtpView viewOTP;

    private Util util;
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up3);
        ButterKnife.bind(this);
        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
        util = new Util(this);
        String phone = getIntent().getStringExtra("phone");
        if (!checkEmptyStrings(phone)) {
            editTextPhone.setText(phone);
        }
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                mVerificationInProgress = false;
                updateUI(STATE_VERIFY_SUCCESS, credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    editTextPhone.setError("Invalid phone number.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.", Snackbar.LENGTH_SHORT).show();
                }
                updateUI(STATE_VERIFY_FAILED);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                startTimer();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                updateUI(STATE_CODE_SENT);
            }
        };
    }

    @OnClick(R.id.buttonSignUp)
    public void onSignUpClicked() {
        if (!validatePhoneNumber()) {
            return;
        }
        if (viewOTP.getText() == null || checkEmptyStrings(viewOTP.getText().toString().trim())) {
            util.toast(getString(R.string.message_add_otp));
        } else {
            String code = viewOTP.getText().toString();
            if (TextUtils.isEmpty(code)) {
                util.toast("OTP Cannot be empty.");
                return;
            } else if (code.length() != 6) {
                util.toast("Invalid OTP.");
                return;
            }
            verifyPhoneNumberWithCode(mVerificationId, code);
        }
    }

    @OnClick(R.id.buttonSendCode)
    public void onResendClicked() {
        if (mVerificationId == null) {
            if (!validatePhoneNumber()) {
                return;
            }
            startPhoneNumberVerification(editTextPhone.getText().toString());
        } else {
            resendVerificationCode(editTextPhone.getText().toString(), mResendToken);
        }
    }

    private void startTimer() {
        CountDownTimer timer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(final long millSecondsLeftToFinish) {
                String time = String.valueOf(millSecondsLeftToFinish / 1000);
                textView.setText(getString(R.string.resend_code_text, time));
                textView.setVisibility(View.VISIBLE);
                buttonSendCode.setVisibility(View.GONE);
            }

            @Override
            public void onFinish() {
                textView.setVisibility(View.GONE);
                buttonSendCode.setVisibility(View.VISIBLE);
                buttonSendCode.setText(getString(R.string.resend_code));
            }
        };
        timer.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                util.toast("Invalid code.");
                                viewOTP.setText("");
                            }
                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
                buttonSendCode.setText(getString(R.string.send_code));
                enableViews(buttonSendCode);
                disableViews(textView, linearOTP);
                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                enableViews(linearOTP);
                disableViews(textView);
                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                buttonSendCode.setText(getString(R.string.resend_code));
                enableViews(buttonSendCode, linearOTP);
                util.toast(getString(R.string.message_verification_failed));
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                disableViews(linearOTP, buttonSendCode, editTextPhone, buttonSignUp);
                // Set the verification text based on the credential
                if (cred != null) {
                    Log.d("cred", "cred");
                    if (cred.getSmsCode() != null) {
                        viewOTP.setText(cred.getSmsCode());
                    }
                }

                break;
            case STATE_SIGNIN_FAILED:
                Log.d("STATE_SIGNIN_FAILED", "STATE_SIGNIN_FAILED");
                // No-op, handled by sign-in check
                break;
            case STATE_SIGNIN_SUCCESS:
                Log.d("STATE_SIGNIN_FAILED", "STATE_SIGNIN_FAILED");
                // Np-op, handled by sign-in check
                break;
        }

        if (user != null) {
            uploadFile(Uri.fromFile(new File(getIntent().getStringExtra("uriPath"))), user.getPhoneNumber());
        }
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = editTextPhone.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            editTextPhone.setError(getString(R.string.invalid_phone));
            return false;
        }

        return true;
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    private void uploadFile(Uri filePath, final String phoneNumber) {
        //if there is a file to upload
        if (filePath != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageReference = storage.getReference();
            //displaying a progress dialog while upload is going on
            util.showLoading(getString(R.string.message_creating_profile));

            final StorageReference riversRef = storageReference.child("images/" + filePath.toString().substring(filePath.toString().lastIndexOf("/") + 1));
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successful
                            riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                @Override
                                public void onSuccess(Uri uri) {
                                    //Handle whatever you're going to do with the URL here
                                    String firstName = getIntent().getStringExtra(getString(R.string.firstName));
                                    String lastName = getIntent().getStringExtra(getString(R.string.last_name));
                                    String gender = getIntent().getStringExtra(getString(R.string.gender_label));
                                    String dob = getIntent().getStringExtra(getString(R.string.dob));
                                    String password = getIntent().getStringExtra(getString(R.string.password_label));
                                    String uriPath = uri.toString();
                                    UserImpl userImplementation = new UserImpl(SignUpThreeActivity.this);
                                    FirestoreUserModel firestoreUserModel = new FirestoreUserModel();
                                    firestoreUserModel.setFirstName(firstName);
                                    firestoreUserModel.setLastName(lastName);
                                    firestoreUserModel.setDob(dob);
                                    firestoreUserModel.setGender(gender);
                                    firestoreUserModel.setPassword(password);
                                    firestoreUserModel.setPhone(phoneNumber);
                                    firestoreUserModel.setUriPath(uriPath);
                                    util.hideLoading();
                                    userImplementation.doesUserExist(phoneNumber, firestoreUserModel);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successful
                            util.hideLoading();
                            util.toast(exception.getMessage());
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            util.log("Progress", "" + progress);

                        }
                    });
        }
        //if there is not any file
        else {
            //you can display an error toast
            util.toast(getString(R.string.message_picture_empty));
        }
    }


}
