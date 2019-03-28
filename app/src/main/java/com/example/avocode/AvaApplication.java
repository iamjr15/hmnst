package com.example.avocode;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.google.firebase.firestore.FirebaseFirestore;
import com.orm.SugarApp;
import com.orm.SugarContext;

public class AvaApplication extends SugarApp {
    private FirebaseFirestore db;
    public static AvaApplication instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(this);
        instance = this;
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public FirebaseFirestore getDbInstance() {
        return db;
    }

    public static AvaApplication getInstance() {
        if (null == instance) {
            instance = new AvaApplication();
        }
        return instance;
    }
}
