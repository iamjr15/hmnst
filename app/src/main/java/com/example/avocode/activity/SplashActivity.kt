package com.example.avocode.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

import com.example.avocode.R
import com.example.avocode.utils.Util

//Splash screen
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val handler = Handler()
        handler.postDelayed({
            val util = Util(this@SplashActivity)
            var intent = Intent(this@SplashActivity, MainActivity::class.java)
            if (util.isUseerLoggedIn) {
                intent = Intent(this@SplashActivity, HomeActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 3000)
    }
}
