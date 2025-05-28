package com.growsmart.mobile

import android.os.Bundle
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.Description
import com.google.firebase.database.*
import android.graphics.Color
import android.util.Log

class SuhuActivity : BaseActivity() {

    private lateinit var suhuChart: LineChart
    private lateinit var txtSuhuValue: TextView

    private lateinit var currentSuhuRef: DatabaseReference
    private lateinit var logSuhuRef: DatabaseReference

    private val suhuEntries = ArrayList<Entry>()

    override fun getLayoutResourceId(): Int = R.layout.activity_suhu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Grafik Suhu")

        suhuChart = findViewById(R.id.suhuChart)
        txtSuhuValue = findViewById(R.id.txtSuhuValue)

        val database = FirebaseDatabase.getInstance()
        currentSuhuRef = database.getReference("sensor/suhu")
        logSuhuRef = database.getReference("GrowSmart/log/suhu")




        bacaSuhuSekarang()
        tampilkanGrafikSuhu()
    }

    private fun bacaSuhuSekarang() {
        currentSuhuRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val suhuStr = snapshot.getValue(String::class.java)
                val suhu = suhuStr?.trim()?.toFloatOrNull()

                if (suhu != null) {
                    txtSuhuValue.text = "Suhu: $suhu °C"
                } else {
                    txtSuhuValue.text = "Data suhu tidak valid"
                    Log.e("SuhuActivity", "Gagal parsing suhu: '$suhuStr'")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                txtSuhuValue.text = "Gagal membaca suhu"
                Log.e("SuhuActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun tampilkanGrafikSuhu() {
        logSuhuRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                suhuEntries.clear()
                var index = 0f

                for (data in snapshot.children) {
                    val valueStr = data.getValue(String::class.java)
                    val value = valueStr?.toFloatOrNull()
                    if (value != null) {
                        suhuEntries.add(Entry(index, value))
                        index += 1f
                    }
                }

                val dataSet = LineDataSet(suhuEntries, "Suhu (°C)").apply {
                    color = Color.BLUE
                    valueTextColor = Color.BLACK
                    lineWidth = 2f
                    circleRadius = 4f
                    setCircleColor(Color.BLUE)
                    setDrawValues(false)
                }

                suhuChart.data = LineData(dataSet)
                suhuChart.description = Description().apply {
                    text = "Riwayat Suhu (°C)"
                    textColor = Color.DKGRAY
                }
                suhuChart.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SuhuActivity", "Gagal membaca log suhu: ${error.message}")
            }
        })
    }
}