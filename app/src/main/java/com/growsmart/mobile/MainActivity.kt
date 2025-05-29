package com.growsmart.mobile

import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.Button
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.AdapterView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.core.view.GravityCompat
import android.view.View
import com.google.firebase.database.*
import android.util.Log
import android.graphics.Color
import android.widget.Toast
import android.widget.TextView
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat







class MainActivity : BaseActivity() {

    private lateinit var chart: LineChart
    private lateinit var txtSuhu: TextView
    private lateinit var txtPh: TextView
    private lateinit var txtTds: TextView

    private lateinit var database: FirebaseDatabase
    private lateinit var sensorRef: DatabaseReference
    private lateinit var logRef: DatabaseReference

    override fun getLayoutResourceId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        chart = findViewById(R.id.chartSuhuPreview)
        txtSuhu = findViewById(R.id.txtSuhu)
        txtPh = findViewById(R.id.txtPh)
        txtTds = findViewById(R.id.txtTds)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val btnManual = findViewById<Button>(R.id.btnManualControl)
        btnManual.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val spinner = findViewById<Spinner>(R.id.spinnerGrafik)
        val grafikOptions = arrayOf("Suhu", "pH", "Nutrisi")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grafikOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        database = FirebaseDatabase.getInstance()
        sensorRef = database.getReference("sensor")
        logRef = database.getReference("GrowSmart/log")

        tampilkanNilaiSensor()
        pantauPeringatan()

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val kategori = when (grafikOptions[position]) {
                    "Suhu" -> "suhu"
                    "pH" -> "ph"
                    "Nutrisi" -> "tds"
                    else -> ""
                }
                tampilkanGrafikDariLog(kategori)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun tampilkanNilaiSensor() {
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val suhu = snapshot.child("suhu").getValue(String::class.java)
                val ph = snapshot.child("ph").getValue(String::class.java)
                val tds = snapshot.child("tds").getValue(String::class.java)

                txtSuhu.text = "Suhu: ${suhu ?: "--"} °C"
                txtPh.text = "pH: ${ph ?: "--"}"
                txtTds.text = "TDS: ${tds ?: "--"} ppm"

                Log.d("FirebaseSensor", "suhu=$suhu, ph=$ph, tds=$tds")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseSensor", "Gagal ambil data sensor: ${error.message}")
            }
        })
    }

    private fun tampilkanGrafikDariLog(kategori: String) {
        val entries = ArrayList<Entry>()
        val ref = logRef.child(kategori)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                entries.clear()
                var index = 0f

                for (data in snapshot.children) {
                    val value = data.getValue(String::class.java)?.toFloatOrNull()
                    if (value != null) {
                        entries.add(Entry(index++, value))
                    }
                }

                if (entries.isEmpty()) {
                    chart.clear()
                    chart.invalidate()
                    Toast.makeText(this@MainActivity, "Data $kategori kosong", Toast.LENGTH_SHORT).show()
                    return
                }

                val label = when (kategori) {
                    "suhu" -> "Suhu (°C)"
                    "ph" -> "pH"
                    "tds" -> "Nutrisi (ppm)"
                    else -> "Data"
                }

                val dataSet = LineDataSet(entries, label).apply {
                    color = Color.BLUE
                    setCircleColor(Color.RED)
                    lineWidth = 2f
                    circleRadius = 4f
                    setDrawValues(false)
                }

                chart.data = LineData(dataSet)
                chart.description.text = "Grafik $label"
                chart.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseLog", "Gagal ambil grafik $kategori: ${error.message}")
            }
        })
    }

    private fun pantauPeringatan() {
        val refPh = database.getReference("GrowSmart/status/peringatan/ph")
        val refNutrisi = database.getReference("GrowSmart/status/peringatan/nutrisi")

        refPh.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                if (status != null && status != "Normal") {
                    tampilkanNotifikasi("Peringatan pH", status)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        refNutrisi.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                if (status != null && status != "Normal") {
                    tampilkanNotifikasi("Peringatan Nutrisi", status)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun tampilkanNotifikasi(judul: String, isi: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "PERINGATAN_SENSOR")
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle(judul)
            .setContentText(isi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Peringatan Sensor"
            val descriptionText = "Notifikasi untuk pH dan Nutrisi"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("PERINGATAN_SENSOR", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}