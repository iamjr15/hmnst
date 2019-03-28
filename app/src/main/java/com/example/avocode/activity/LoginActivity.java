package com.example.avocode.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;

import com.example.avocode.R;
import com.example.avocode.repo.UserImpl;
import com.example.avocode.utils.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
//Login activity where user can login to the app using phone number and password. Data will be checked from firestore
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.phone_et)
    EditText phone_et;
    @BindView(R.id.password_et)
    EditText password_et;

    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        util = new Util(this);
    }

    @OnClick(R.id.next_btn)
    public void onNext() {
        if (TextUtils.isEmpty(phone_et.getText().toString())) {
            phone_et.setError(getString(R.string.message_phone_required));
            phone_et.requestFocus();
        } else if (TextUtils.isEmpty(password_et.getText().toString())) {
            password_et.setError(getString(R.string.message_password_required));
            password_et.requestFocus();
        } else {
            UserImpl userImplementation = new UserImpl(LoginActivity.this);
            userImplementation.getLoginUserByPhone(phone_et.getText().toString().trim(), password_et.getText().toString().trim());
        }
    }
}
