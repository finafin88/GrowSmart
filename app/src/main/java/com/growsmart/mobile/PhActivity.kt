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
    private var isPhUpOn = false
    private var isPhDownOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Grafik pH")

        val phChart = findViewById<LineChart>(R.id.phChart)
        val btnPhUp = findViewById<Button>(R.id.btnPhUp)
        val btnPhDown = findViewById<Button>(R.id.btnPhDown)

    val dummyEntries = listOf(
            Entry(0f, 6.5f),
            Entry(1f, 6.8f),
            Entry(2f, 7.0f),
            Entry(3f, 6.9f)
        )
        val dataSet = LineDataSet(dummyEntries, "pH Air")
        phChart.data = LineData(dataSet)
        phChart.invalidate()

        updatePhUpButton(btnPhUp)
        updatePhDownButton(btnPhDown)

        btnPhUp.setOnClickListener {
            isPhUpOn = !isPhUpOn
            updatePhUpButton(btnPhUp)
        }
        btnPhDown.setOnClickListener {
            isPhDownOn = !isPhDownOn
            updatePhDownButton(btnPhDown)
        }
    }
    private fun updatePhUpButton(button: Button) {
        if (isPhUpOn) {
            button.text = "PH Up ON"
            button.setBackgroundResource(R.drawable.bg_button_on)
        } else {
            button.text = "PH Up OFF"
            button.setBackgroundResource(R.drawable.bg_button_off)
        }
    }
    private fun updatePhDownButton(button: Button) {
        if (isPhDownOn) {
            button.text = "PH Down ON"
            button.setBackgroundResource(R.drawable.bg_button_on)
        } else {
            button.text = "PH Down OFF"
            button.setBackgroundResource(R.drawable.bg_button_off)
        }
    }
}

