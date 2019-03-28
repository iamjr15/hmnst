package com.example.avocode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.example.avocode.AvaApplication;
import com.example.avocode.R;
import com.example.avocode.config.Constants;
import com.example.avocode.utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.avocode.utils.Util.checkEmptyStrings;

//First sign up screen to take user's basic info
public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.phonenumber_et)
    EditText phonenumber_et;

    @BindView(R.id.passwordSignUp_et)
    EditText passwordSignUp_et;

    @BindView(R.id.firstname_et)
    EditText firstname_et;

    @BindView(R.id.lastname_et)
    EditText lastname_et;

    @BindView(R.id.dob_et)
    EditText dob_et;

    @BindView(R.id.gender_et)
    EditText gender_et;

    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        util = new Util(this);
    }

    @OnClick(R.id.next_signup_button)
    public void onNextClicked() {
        if (checkEmptyStrings(firstname_et.getText().toString())) {
            firstname_et.setError(getString(R.string.message_first_name_required));
            firstname_et.requestFocus();
        } else if (checkEmptyStrings(lastname_et.getText().toString())) {
            lastname_et.setError(getString(R.string.message_last_name_required));
            lastname_et.requestFocus();
        } else if (checkEmptyStrings(passwordSignUp_et.getText().toString())) {
            passwordSignUp_et.setError(getString(R.string.message_password_required));
            passwordSignUp_et.requestFocus();
        } else if (checkEmptyStrings(phonenumber_et.getText().toString())) {
            phonenumber_et.setError(getString(R.string.message_phone_required));
            phonenumber_et.requestFocus();
        } else {
            util.showLoading(getString(R.string.please_wait));
            FirebaseFirestore db = AvaApplication.getInstance().getDbInstance();
            Task<DocumentSnapshot> docSnapshot = db.collection(Constants.USER_COLLECTION).document(phonenumber_et.getText().toString()).get();
            docSnapshot.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // user already exist in database
                            util.toast(getString(R.string.message_user_already_exist));
                        } else {
                            //user does not exist in database
                            addNewRegisteredUser();
                        }
                        util.hideLoading();
                    } else {
                        //user does not exist in database
                        addNewRegisteredUser();
                    }
                }
            });

        }
    }

    private void addNewRegisteredUser() {
        util.hideLoading();
        Intent intent = new Intent(SignUpActivity.this, SignUpTwoActivity.class);
        intent.putExtra("firstName", firstname_et.getText().toString().trim());
        intent.putExtra("lastName", lastname_et.getText().toString().trim());
        intent.putExtra("gender", gender_et.getText().toString().trim());
        intent.putExtra("dob", dob_et.getText().toString().trim());
        intent.putExtra("password", passwordSignUp_et.getText().toString().trim());
        intent.putExtra("phone", phonenumber_et.getText().toString().trim());
        startActivity(intent);
    }

}
