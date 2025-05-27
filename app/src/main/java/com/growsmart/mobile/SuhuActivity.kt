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

    private lateinit var suhuRef: DatabaseReference

    private val suhuEntries = ArrayList<Entry>()
    private var indexCounter = 0f

    override fun getLayoutResourceId(): Int = R.layout.activity_suhu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Grafik Suhu")

        suhuChart = findViewById(R.id.suhuChart)
        txtSuhuValue = findViewById(R.id.txtSuhuValue)

        suhuRef = FirebaseDatabase.getInstance().getReference("sensor/suhu")


        suhuRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val suhuStr = snapshot.getValue(String::class.java)
                val suhu = suhuStr?.trim()?.toFloatOrNull()

                if (suhu != null) {
                    txtSuhuValue.text = "Suhu: $suhuStr °C"
                    suhuEntries.add(Entry(indexCounter, suhu))
                    indexCounter += 1f

                    val dataSet = LineDataSet(suhuEntries, "Suhu (°C)").apply {
                        color = Color.RED
                        valueTextColor = Color.BLACK
                        lineWidth = 2f
                        circleRadius = 4f
                        setCircleColor(Color.RED)
                        setDrawValues(true)
                    }

                    val lineData = LineData(dataSet)
                    suhuChart.data = lineData
                    suhuChart.data.notifyDataChanged()
                    suhuChart.notifyDataSetChanged()

                    suhuChart.description = Description().apply {
                        text = "Data Suhu (°C)"
                        textColor = Color.DKGRAY
                    }

                    suhuChart.invalidate()
                } else {
                    txtSuhuValue.text = "Format suhu tidak valid"
                    Log.e("SuhuActivity", "Gagal parsing suhu: '$suhuStr'")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                txtSuhuValue.text = "Gagal membaca suhu"
            }
        })
    }
}