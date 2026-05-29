package com.example.tvapp

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class SearchActivity : FragmentActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, SearchFragment())
                .commit()
        }
    }
}
