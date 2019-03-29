package com.example.avocode.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.avocode.AvaApplication;
import com.example.avocode.R;
import com.example.avocode.config.Constants;
import com.example.avocode.utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
    TextView dob_et;

    @BindView(R.id.rbMale)
    RadioButton rbMale;

    @BindView(R.id.rbFemale)
    RadioButton rbFemale;

    @BindView(R.id.gender_et)
    RadioGroup genderEt;


    private Util util;
    final Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        util = new Util(this);


        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateText();
            }

        };

        dob_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SignUpActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    void updateText() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dob_et.setText(sdf.format(myCalendar.getTime()));
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

    String gender;

    private void addNewRegisteredUser() {
        util.hideLoading();
        gender = rbMale.isChecked() ?
                getString(R.string.male) :
                getString(R.string.female);

        Intent intent = new Intent(SignUpActivity.this, SignUpTwoActivity.class);
        intent.putExtra("firstName", firstname_et.getText().toString().trim());
        intent.putExtra("lastName", lastname_et.getText().toString().trim());
        intent.putExtra("gender", gender);
        intent.putExtra("dob", dob_et.getText().toString().trim());
        intent.putExtra("password", passwordSignUp_et.getText().toString().trim());
        intent.putExtra("phone", phonenumber_et.getText().toString().trim());
        startActivity(intent);
    }

}
