package com.growsmart.mobile

import android.os.Bundle

class SuhuActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_main)
        setToolbarTitle("Suhu")
    }
}