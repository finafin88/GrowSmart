package com.growsmart.mobile

import android.os.Bundle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.FirebaseDatabase
import android.widget.Button
import android.widget.Toast


class NutrisiActivity : BaseActivity() {

    private lateinit var nutrisiChart: LineChart

    private fun kirimPerintahNutrisi(perintah: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("kontrol_manual").setValue(perintah)
            .addOnSuccessListener {
                Toast.makeText(this, "Perintah dikirim: $perintah", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal kirim perintah", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_nutrisi
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Nutrisi")

        nutrisiChart = findViewById(R.id.nutrisiChart)

        // Dummy data nutrisi ganti ke firebase
        val dummyTds = listOf(
            Entry(0f, 700f),
            Entry(1f, 800f),
            Entry(2f, 900f),
            Entry(3f, 870f)
        )

        val dataSet = LineDataSet(dummyTds, "TDS (ppm)")
        val lineData = LineData(dataSet)
        nutrisiChart.data = lineData
        nutrisiChart.invalidate()
        val btnNutrisiA = findViewById<Button>(R.id.btnNutrisiA)
        val btnNutrisiB = findViewById<Button>(R.id.btnNutrisiB)

        btnNutrisiA.setOnClickListener {
            kirimPerintahNutrisi("nutrisi_a")
        }

        btnNutrisiB.setOnClickListener {
            kirimPerintahNutrisi("nutrisi_b")
        }

        }

    }

