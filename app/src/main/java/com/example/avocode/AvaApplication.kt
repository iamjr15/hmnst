package com.example.avocode

import android.content.Context
import android.support.multidex.MultiDex

import com.google.firebase.firestore.FirebaseFirestore
import com.orm.SugarApp
import com.orm.SugarContext

class AvaApplication : SugarApp() {
    var dbInstance: FirebaseFirestore? = null
        private set

    override fun onCreate() {
        super.onCreate()
        SugarContext.init(this)
        instance = this
        dbInstance = FirebaseFirestore.getInstance()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {
        lateinit var instance: AvaApplication
    }
}
