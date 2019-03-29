package com.example.avocode.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;

import com.example.avocode.R;
import com.example.avocode.repo.UserImpl;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//Login activity where user can login to the app using phone number and password. Data will be checked from firestore
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.editTextPhone)
    EditText editTextPhone;
    @BindView(R.id.editTextPassword)
    EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.buttonNext)
    public void onNext() {
        if (TextUtils.isEmpty(editTextPhone.getText().toString())) {
            editTextPhone.setError(getString(R.string.message_phone_required));
            editTextPhone.requestFocus();
        } else if (TextUtils.isEmpty(editTextPassword.getText().toString())) {
            editTextPassword.setError(getString(R.string.message_password_required));
            editTextPassword.requestFocus();
        } else {
            UserImpl userImplementation = new UserImpl(LoginActivity.this);
            userImplementation.getLoginUserByPhone("+91" + editTextPhone.getText().toString().trim(), editTextPassword.getText().toString().trim());
        }
    }
}
