package com.example.avocode.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.example.avocode.R;
import com.example.avocode.utils.Util;

//Splash screen
public class SplashActivity extends AppCompatActivity {

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Util util = new Util(SplashActivity.this);
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                if (util.isUseerLoggedIn()) {
                    intent = new Intent(SplashActivity.this, HomeActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
