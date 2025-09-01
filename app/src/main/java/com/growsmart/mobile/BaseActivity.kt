package com.growsmart.mobile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.ActionBarDrawerToggle
import android.widget.FrameLayout

abstract class BaseActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navView: NavigationView
    protected lateinit var spinnerTanaman: Spinner
    protected lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_drawer)

        prefs = getSharedPreferences("GrowSmartPrefs", Context.MODE_PRIVATE)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val content: FrameLayout = findViewById(R.id.content_frame)
        layoutInflater.inflate(getLayoutResourceId(), content, true)

        val headerView: View = navView.getHeaderView(0)

        headerView.findViewById<TextView?>(R.id.nav_header_email)?.let { tv ->
            val user = FirebaseAuth.getInstance().currentUser
            tv.text = user?.email ?: "Belum Login"
        }

        spinnerTanaman = headerView.findViewById(R.id.spinnerTanaman)

        val daftarTanaman = arrayOf("Selada", "Kangkung", "Pakcoy")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, daftarTanaman)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTanaman.adapter = adapter

        val tanaman = prefs.getString("tanaman_terpilih", "Selada")
        val selectedIndex = adapter.getPosition(tanaman)
        if (selectedIndex >= 0) spinnerTanaman.setSelection(selectedIndex)

        spinnerTanaman.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View?, position: Int, id: Long
            ) {
                val dipilih = daftarTanaman[position]
                prefs.edit().putString("tanaman_terpilih", dipilih).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home   -> startActivity(Intent(this, MainActivity::class.java))
            R.id.nav_suhu   -> startActivity(Intent(this, SuhuActivity::class.java))
            R.id.nav_ph     -> startActivity(Intent(this, PhActivity::class.java))
            R.id.nav_nutrisi-> startActivity(Intent(this, NutrisiActivity::class.java))
            R.id.nav_katup  -> startActivity(Intent(this, KatupActivity::class.java))
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Logout")
                    .setMessage("Apakah Anda yakin ingin logout?")
                    .setPositiveButton("Ya") { dialog, _ ->
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        dialog.dismiss()
                    }
                    .setNegativeButton("Tidak") { d, _ -> d.dismiss() }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ===== Child activity harus mengembalikan layout mereka =====
    abstract fun getLayoutResourceId(): Int

    fun setToolbarTitle(title: String) { supportActionBar?.title = title }

    // (opsional) tema dari prefs — panggil jika kamu pakai
    protected fun applyThemeFromPreferences() {
        val pref = getSharedPreferences("settings", Context.MODE_PRIVATE)
        when (pref.getString("theme_mode", "system")) {
            "dark"  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else    -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
