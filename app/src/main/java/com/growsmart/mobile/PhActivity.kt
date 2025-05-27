package com.growsmart.mobile

import android.os.Bundle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.FirebaseDatabase
import android.widget.Button
import android.widget.Toast


class PhActivity : BaseActivity() {

    private lateinit var phChart: LineChart

    private fun kirimPerintahPH(perintah: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("kontrol_manual").setValue(perintah)
            .addOnSuccessListener {
                Toast.makeText(this, "Perintah terkirim: $perintah", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal kirim perintah", Toast.LENGTH_SHORT).show()
            }
    }


    override fun getLayoutResourceId(): Int {
        return R.layout.activity_ph
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Grafik pH")

        phChart = findViewById(R.id.phChart)

        // Dummy data ph ganti ke firebase
        val dummyEntries = listOf(
            Entry(0f, 6.5f),
            Entry(1f, 6.8f),
            Entry(2f, 7.0f),
            Entry(3f, 6.9f)
        )

        val dataSet = LineDataSet(dummyEntries, "Dummy pH")
        val lineData = LineData(dataSet)
        phChart.data = lineData
        phChart.invalidate()
        val btnPhUp = findViewById<Button>(R.id.btnPhUp)
        val btnPhDown = findViewById<Button>(R.id.btnPhDown)

        btnPhUp.setOnClickListener {
            kirimPerintahPH("ph_up")
        }

        btnPhDown.setOnClickListener {
            kirimPerintahPH("ph_down")
        }
    }

}

