package com.example.avocode.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.example.avocode.R
import kotlinx.android.synthetic.main.activity_main.*

// First screen to select login or sign up
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonSignUp.setOnClickListener {
            val intent = Intent(this@MainActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
        buttonSignIn.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
