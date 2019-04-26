package com.candroid.lacedboots

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.candroid.bootlaces.Laces

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Laces.tie(this, MyService::class.java.name, notificationIcon = android.R.drawable.status_bar_item_background)
    }
}
