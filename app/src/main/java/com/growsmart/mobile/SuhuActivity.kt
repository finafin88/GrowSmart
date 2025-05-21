package com.growsmart.mobile

import android.os.Bundle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class SuhuActivity : BaseActivity() {

    private lateinit var suhuChart: LineChart

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_suhu
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Grafik Suhu")

        suhuChart = findViewById(R.id.suhuChart)

        // Dummy data suhu ganti ke firebase
        val dummySuhu = listOf(
            Entry(0f, 25.0f),
            Entry(1f, 26.3f),
            Entry(2f, 27.1f),
            Entry(3f, 26.7f)
        )

        val dataSet = LineDataSet(dummySuhu, "Suhu (°C)")
        val lineData = LineData(dataSet)
        suhuChart.data = lineData
        suhuChart.invalidate()
    }
}
