package com.example.tvapp

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.example.tvapp.data.AppDatabase

class MainActivity : FragmentActivity(R.layout.activity_main) {

    lateinit var database: AppDatabase
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = AppDatabase.getInstance(applicationContext)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.main_container, MainFragment())
            }
        }
    }
}
