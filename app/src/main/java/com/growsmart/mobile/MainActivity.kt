package com.growsmart.mobile

import android.content.res.ColorStateList
import android.os.Bundle
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
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : BaseActivity() {

    private lateinit var chart: LineChart
    private lateinit var txtSuhu: TextView
    private lateinit var txtPh: TextView
    private lateinit var txtTds: TextView
    private lateinit var btnModeToggle: Button
    private var isManualMode = false

    private lateinit var database: FirebaseDatabase
    private lateinit var sensorRef: DatabaseReference
    private lateinit var logRef: DatabaseReference
    private lateinit var modeRef: DatabaseReference

    override fun getLayoutResourceId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        chart = findViewById(R.id.chartSuhuPreview)
        txtSuhu = findViewById(R.id.txtSuhu)
        txtPh = findViewById(R.id.txtPh)
        txtTds = findViewById(R.id.txtTds)

        drawerLayout = findViewById(R.id.drawer_layout)
        btnModeToggle = findViewById(R.id.btnModeToggle)

        database = FirebaseDatabase.getInstance()
        sensorRef = database.getReference("GrowSmart/sensor")
        logRef = database.getReference("GrowSmart/log")
        modeRef = database.getReference("perintah/mode")


        val btnModeToggle = findViewById<Button>(R.id.btnModeToggle)


        btnModeToggle.setOnClickListener {
            isManualMode = !isManualMode
            val newMode = if (isManualMode) "manual" else "otomatis"

            modeRef.setValue(newMode)
                .addOnSuccessListener {
                    Toast.makeText(this, "Mode diubah ke: $newMode", Toast.LENGTH_SHORT).show()
                    
                    if (newMode == "manual") {
                        drawerLayout.openDrawer(GravityCompat.START)
                    }

                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal mengubah mode", Toast.LENGTH_SHORT).show()
                }
        }

        val spinner = findViewById<Spinner>(R.id.spinnerGrafik)
        val grafikOptions = arrayOf("Suhu", "pH", "Nutrisi")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grafikOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter


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

        tampilkanNilaiSensor()
        tampilkanModeFirebase()
        pantauPeringatan()
    }

    private fun tampilkanModeFirebase() {
        modeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mode = snapshot.getValue(String::class.java)
                isManualMode = (mode == "manual")

                if (isManualMode) {
                    btnModeToggle.text = "Mode: Manual"
                    btnModeToggle.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK))
                } else {
                    btnModeToggle.text = "Mode: Otomatis"
                    btnModeToggle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal ambil mode", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun tampilkanNilaiSensor() {
        val paths = listOf("suhu", "ph", "tds")
        val textViews = mapOf(
            "suhu" to txtSuhu,
            "ph" to txtPh,
            "tds" to txtTds
        )

        for (path in paths) {
            logRef.child(path).limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var lastValue = "--"
                    for (data in snapshot.children) {
                        lastValue = data.getValue(String::class.java) ?: "--"
                    }

                    when (path) {
                        "suhu" -> textViews["suhu"]?.text = "Suhu: $lastValue °C"
                        "ph" -> textViews["ph"]?.text = "pH: $lastValue"
                        "tds" -> textViews["tds"]?.text = "TDS: $lastValue ppm"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseLog", "Gagal ambil data $path: ${error.message}")
                }
            })
        }
    }


    private fun tampilkanGrafikDariLog(kategori: String) {
        val entries = ArrayList<Entry>()
        val query = logRef.child(kategori).limitToLast(30)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                entries.clear()

                val sorted = snapshot.children.sortedBy { it.key?.toLongOrNull() }

                for (data in sorted) {
                    val x = data.key?.toFloatOrNull() ?: continue
                    val y = data.getValue(String::class.java)?.toFloatOrNull() ?: continue
                    entries.add(Entry(x, y))
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
                    mode = LineDataSet.Mode.LINEAR
                }

                chart.data = LineData(dataSet)

                chart.xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val millis = value.toLong() * 1000
                        val date = Date(millis)

                        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                        return format.format(date)
                    }
                }

                val yAxis = chart.axisLeft
                when (kategori) {
                    "suhu" -> {
                        yAxis.axisMinimum = 15f
                        yAxis.axisMaximum = 40f
                    }
                    "ph" -> {
                        yAxis.axisMinimum = 0f
                        yAxis.axisMaximum = 14f
                    }
                    "tds" -> {
                        yAxis.axisMinimum = 0f
                        yAxis.axisMaximum = 1000f
                    }
                }
                chart.axisRight.isEnabled = false

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