package com.example.avocode.repo;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.avocode.AvaApplication;
import com.example.avocode.R;
import com.example.avocode.activity.HomeActivity;
import com.example.avocode.config.Constants;
import com.example.avocode.models.FirestoreUserModel;
import com.example.avocode.utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

//helper class for login and registration purpose
public class UserImpl implements IUserRepository {

    private static final String TAG = UserImpl.class.getSimpleName();

    private Activity activity;

    private FirebaseFirestore db;

    private AvaApplication app;
    private Util util;

    public UserImpl(Activity activity) {
        this.activity = activity;
        app = AvaApplication.getInstance();
        db = app.getDbInstance();
        util = new Util(activity);
    }

    //Check user exist or not
    @Override
    public void doesUserExist(String phone, final FirestoreUserModel firestoreUserModel) {
        util.showLoading(activity.getString(R.string.message_creating_profile));
        Task<DocumentSnapshot> docSnapshot = db.collection(Constants.USER_COLLECTION).document(phone).get();
        docSnapshot.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // user already exist in database
                        util.toast(activity.getString(R.string.message_user_already_exist));
                        util.hideLoading();
                    } else {
                        //user does not exist in database
                        addNewRegisteredUser(firestoreUserModel);
                    }
                } else {
                    //user does not exist in database
                    addNewRegisteredUser(firestoreUserModel);
                }
            }
        });
    }

    //Add user
    @Override
    public void addNewRegisteredUser(final FirestoreUserModel firestoreUserModel) {
        Map<String, Object> user = new HashMap<>();
        user.put(Constants.DocumentFields.FIRST_NAME, firestoreUserModel.getFirstName());
        user.put(Constants.DocumentFields.LAST_NAME, firestoreUserModel.getLastName());
        user.put(Constants.DocumentFields.PASSWORD, firestoreUserModel.getPassword());
        user.put(Constants.DocumentFields.DOB, firestoreUserModel.getDob());
        user.put(Constants.DocumentFields.GENDER, firestoreUserModel.getGender());
        user.put(Constants.DocumentFields.PHONE, firestoreUserModel.getPhone());
        user.put(Constants.DocumentFields.URI_PATH, firestoreUserModel.getUriPath());
        Task<Void> newUser = db.collection(Constants.USER_COLLECTION).document(firestoreUserModel.getPhone()).set(user);
        newUser.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "User was successfully added");
                util.saveUser(firestoreUserModel.getFirstName(), firestoreUserModel.getLastName(), firestoreUserModel.getGender(), firestoreUserModel.getDob(), firestoreUserModel.getPhone(), firestoreUserModel.getUriPath());
                Intent intent = new Intent(activity, HomeActivity.class);
                intent.putExtra(activity.getString(R.string.firstName), firestoreUserModel.getFirstName());
                intent.putExtra(activity.getString(R.string.lastName), firestoreUserModel.getLastName());
                intent.putExtra(activity.getString(R.string.gender_label), firestoreUserModel.getGender());
                intent.putExtra(activity.getString(R.string.dob), firestoreUserModel.getDob());
                intent.putExtra(activity.getString(R.string.password_label), firestoreUserModel.getPassword());
                intent.putExtra(activity.getString(R.string.phone_label), firestoreUserModel.getPhone());
                intent.putExtra(activity.getString(R.string.uriPath), firestoreUserModel.getUriPath());
                util.hideLoading();
                // set the new task and clear flags
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                util.hideLoading();
                Log.d(TAG, "Error has occured " + e.getMessage());
                util.toast(activity.getString(R.string.message_user_registration_failed));
            }
        });
    }

    //Login user
    @Override
    public void getLoginUserByPhone(String phone, String password) {
        util.showLoading(activity.getString(R.string.message_logging));
        db.collection(Constants.USER_COLLECTION)
                .whereEqualTo(Constants.DocumentFields.PHONE, phone)
                .whereEqualTo(Constants.DocumentFields.PASSWORD, password).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.d("onEvent", "onEvent");
                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    FirestoreUserModel firestoreUserModel = null;
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        firestoreUserModel = snapshot.toObject(FirestoreUserModel.class);
                    }
                    if (firestoreUserModel != null) {
                        util.saveUser(firestoreUserModel.getFirstName(), firestoreUserModel.getLastName(), firestoreUserModel.getGender(), firestoreUserModel.getDob(), firestoreUserModel.getPhone(), firestoreUserModel.getUriPath());
                        Intent intent = new Intent(activity, HomeActivity.class);
                        intent.putExtra(activity.getString(R.string.firstName), firestoreUserModel.getFirstName());
                        intent.putExtra(activity.getString(R.string.last_name), firestoreUserModel.getLastName());
                        intent.putExtra(activity.getString(R.string.gender_label), firestoreUserModel.getGender());
                        intent.putExtra(activity.getString(R.string.dob), firestoreUserModel.getDob());
                        intent.putExtra(activity.getString(R.string.password_label), firestoreUserModel.getPassword());
                        intent.putExtra(activity.getString(R.string.phone_label), firestoreUserModel.getPhone());
                        intent.putExtra(activity.getString(R.string.uriPath), firestoreUserModel.getUriPath());
                        // set the new task and clear flags
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        util.hideLoading();
                        activity.startActivity(intent);
                    } else {
                        util.hideLoading();
                        util.toast(activity.getString(R.string.message_credential_invalid));
                    }
                } else {
                    util.hideLoading();
                    util.toast(activity.getString(R.string.message_credential_invalid));
                }
            }
        });
    }
}
