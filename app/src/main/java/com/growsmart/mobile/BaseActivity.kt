package com.growsmart.mobile

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.firebase.auth.FirebaseAuth


abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_drawer)
        navView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val contentLayout = findViewById<FrameLayout>(R.id.content_frame)
        layoutInflater.inflate(getLayoutResourceId(), contentLayout, true)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    abstract fun getLayoutResourceId(): Int

    fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }


    fun setContentLayout(layoutResID: Int) {
        val frameLayout = findViewById<FrameLayout>(R.id.content_frame)
        layoutInflater.inflate(layoutResID, frameLayout, true)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }

            R.id.nav_suhu -> startActivity(Intent(this, SuhuActivity::class.java))
            R.id.nav_ph -> startActivity(Intent(this, PhActivity::class.java))
            R.id.nav_nutrisi -> startActivity(Intent(this, NutrisiActivity::class.java))
        }

            drawerLayout.closeDrawer(GravityCompat.START)
                return true
        }
        override
        fun onBackPressed() {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        }
}


