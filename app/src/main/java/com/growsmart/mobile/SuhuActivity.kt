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
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SuhuActivity : BaseActivity() {

    private lateinit var suhuChart: LineChart
    private lateinit var txtSuhuValue: TextView

    private lateinit var logSuhuRef: DatabaseReference
    private val suhuEntries = ArrayList<Entry>()

    override fun getLayoutResourceId(): Int = R.layout.activity_suhu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Grafik Suhu")

        suhuChart = findViewById(R.id.suhuChart)
        txtSuhuValue = findViewById(R.id.txtSuhuValue)

        val database = FirebaseDatabase.getInstance()
        logSuhuRef = database.getReference("GrowSmart/log/suhu")

        tampilkanGrafikDanNilaiSuhu()
    }

    private fun tampilkanGrafikDanNilaiSuhu() {
        logSuhuRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                suhuEntries.clear()

                val sorted = snapshot.children.sortedBy { it.key?.toLongOrNull() }

                for (data in sorted) {
                    val x = data.key?.toFloatOrNull() ?: continue
                    val y = data.getValue(String::class.java)?.toFloatOrNull() ?: continue
                    suhuEntries.add(Entry(x, y))
                }

                if (suhuEntries.isNotEmpty()) {
                    // ✅ Ambil nilai terakhir
                    val latestSuhu = suhuEntries.last().y
                    txtSuhuValue.text = "Nilai Suhu = %.1f °C".format(latestSuhu)

                    val dataSet = LineDataSet(suhuEntries, "Suhu (°C)").apply {
                        color = Color.BLUE
                        valueTextColor = Color.BLACK
                        lineWidth = 2f
                        circleRadius = 4f
                        setCircleColor(Color.BLUE)
                        setDrawValues(false)
                    }

                    suhuChart.data = LineData(dataSet)

                    suhuChart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val millis = value.toLong() * 1000
                            val date = Date(millis)
                            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                            return format.format(date)
                        }
                    }

                    val yAxis = suhuChart.axisLeft
                    yAxis.axisMinimum = 15f
                    yAxis.axisMaximum = 40f
                    suhuChart.axisRight.isEnabled = false

                    suhuChart.description = Description().apply {
                        text = "Riwayat Suhu (°C)"
                        textColor = Color.DKGRAY
                    }
                    suhuChart.invalidate()
                } else {
                    txtSuhuValue.text = "Data suhu tidak tersedia"
                    suhuChart.clear()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SuhuActivity", "Gagal membaca log suhu: ${error.message}")
                txtSuhuValue.text = "Gagal membaca suhu"
            }
        })
    }
}