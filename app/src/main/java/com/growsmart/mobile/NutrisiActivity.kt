package com.growsmart.mobile

import android.os.Bundle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import android.widget.Button
import android.widget.Toast
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.graphics.Color


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
        val dataSet = LineDataSet(dummyTds, "Nutrisi AB (ppm)").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.BLUE)
            setDrawValues(true)
        }

        val lineData = LineData(dataSet)
        nutrisiChart.data = lineData
        nutrisiChart.description.text = "Grafik Nutrisi AB"
        nutrisiChart.invalidate()


        val btnNutrisiAB = findViewById<Button>(R.id.btnNutrisiAB)
        val refNutrisiAB = FirebaseDatabase.getInstance().getReference("manual/nutrisi_ab")

        refNutrisiAB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOn = snapshot.getValue(Boolean::class.java) ?: false
                btnNutrisiAB.text = if (isOn) "Matikan Nutrisi AB" else "Nyalakan Nutrisi AB"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        btnNutrisiAB.setOnClickListener {
            refNutrisiAB.get().addOnSuccessListener { snapshot ->
                val currentState = snapshot.getValue(Boolean::class.java) ?: false
                refNutrisiAB.setValue(!currentState)
            }
        }
    }

    }

