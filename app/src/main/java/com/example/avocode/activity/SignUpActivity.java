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

    @BindView(R.id.editTextPhone)
    EditText editTextPhone;

    @BindView(R.id.editTextPassword)
    EditText editTextPassword;

    @BindView(R.id.editTextFirstName)
    EditText editTextFirstName;

    @BindView(R.id.editTextLastName)
    EditText editTextLastName;

    @BindView(R.id.editTextDOB)
    TextView editTextDOB;

    @BindView(R.id.rbMale)
    RadioButton rbMale;

    @BindView(R.id.rbFemale)
    RadioButton rbFemale;

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

        editTextDOB.setOnClickListener(new View.OnClickListener() {
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
        editTextDOB.setText(sdf.format(myCalendar.getTime()));
    }

    @OnClick(R.id.buttonSignUp)
    public void onNextClicked() {
        if (checkEmptyStrings(editTextFirstName.getText().toString())) {
            editTextFirstName.setError(getString(R.string.message_first_name_required));
            editTextFirstName.requestFocus();
        } else if (checkEmptyStrings(editTextLastName.getText().toString())) {
            editTextLastName.setError(getString(R.string.message_last_name_required));
            editTextLastName.requestFocus();
        } else if (checkEmptyStrings(editTextPassword.getText().toString())) {
            editTextPassword.setError(getString(R.string.message_password_required));
            editTextPassword.requestFocus();
        } else if (checkEmptyStrings(editTextPhone.getText().toString())) {
            editTextPhone.setError(getString(R.string.message_phone_required));
            editTextPhone.requestFocus();
        } else {
            util.showLoading(getString(R.string.please_wait));
            FirebaseFirestore db = AvaApplication.getInstance().getDbInstance();
            Task<DocumentSnapshot> docSnapshot = db.collection(Constants.USER_COLLECTION).document(editTextPhone.getText().toString()).get();
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
        intent.putExtra(getString(R.string.firstName), editTextFirstName.getText().toString().trim());
        intent.putExtra(getString(R.string.last_name), editTextLastName.getText().toString().trim());
        intent.putExtra(getString(R.string.gender_label), gender);
        intent.putExtra(getString(R.string.dob), editTextDOB.getText().toString().trim());
        intent.putExtra(getString(R.string.password_label), editTextPassword.getText().toString().trim());
        intent.putExtra(getString(R.string.phone_label), editTextPhone.getText().toString().trim());
        startActivity(intent);
    }

}
