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


class MainActivity : BaseActivity() {

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_main
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val btnManual = findViewById<Button>(R.id.btnManualControl)

        btnManual.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)

        }
        val spinner = findViewById<Spinner>(R.id.spinnerGrafik)
        val chart = findViewById<LineChart>(R.id.chartSuhuPreview)

        val grafikOptions = arrayOf("Suhu", "pH", "Nutrisi")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grafikOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (grafikOptions[position]) {
                    "Suhu" -> tampilkanGrafikSuhu(chart)
                    "pH" -> tampilkanGrafikPh(chart)
                    "Nutrisi" -> tampilkanGrafikTds(chart)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
    fun tampilkanGrafikSuhu(chart: LineChart) {
        val entries = listOf(
            Entry(0f, 25f),
            Entry(1f, 26f),
            Entry(2f, 27f)
        )
        val dataSet = LineDataSet(entries, "Suhu (°C)")
        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    fun tampilkanGrafikPh(chart: LineChart) {
        val entries = listOf(
            Entry(0f, 6.5f),
            Entry(1f, 6.8f),
            Entry(2f, 7.1f)
        )
        val dataSet = LineDataSet(entries, "pH")
        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    fun tampilkanGrafikTds(chart: LineChart) {
        val entries = listOf(
            Entry(0f, 800f),
            Entry(1f, 820f),
            Entry(2f, 850f)
        )
        val dataSet = LineDataSet(entries, "Nutrisi (ppm)")
        chart.data = LineData(dataSet)
        chart.invalidate()
    }



}