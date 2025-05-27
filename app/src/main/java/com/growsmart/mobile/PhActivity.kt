package com.growsmart.mobile

import android.os.Bundle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.FirebaseDatabase
import android.widget.Button
import android.widget.Toast
import android.widget.TextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import java.util.*
import android.util.Log



class PhActivity : BaseActivity() {

    private lateinit var phChart: LineChart
    private lateinit var txtPhValue: TextView
    private lateinit var txtPhKosong: TextView
    private lateinit var database: DatabaseReference

    private val phEntries = ArrayList<Entry>()
    private var indexCounter = 0f

    override fun getLayoutResourceId(): Int = R.layout.activity_ph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Grafik pH")


        phChart = findViewById(R.id.phChart)
        txtPhValue = findViewById(R.id.txtPhValue)
        txtPhKosong = findViewById(R.id.txtPhKosong)
        val btnPhUp = findViewById<Button>(R.id.btnPhUp)
        val btnPhDown = findViewById<Button>(R.id.btnPhDown)


        database = FirebaseDatabase.getInstance().reference


        btnPhUp.setOnClickListener { kirimPerintahPH("ph_up") }
        btnPhDown.setOnClickListener { kirimPerintahPH("ph_down") }

        tampilkanDataPH()
    }

    private fun kirimPerintahPH(perintah: String) {
        val path = when (perintah) {
            "ph_down" -> "GrowSmart/status/manual/ph_down"
            "ph_up" -> "GrowSmart/status/manual/ph_up"
            else -> return
        }

        database.child(path).setValue(1)
            .addOnSuccessListener {
                Toast.makeText(this, "Perintah terkirim: $perintah", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal kirim perintah", Toast.LENGTH_SHORT).show()
            }
    }

    private fun tampilkanDataPH() {
        database.child("sensor/ph").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nilaiPhString = snapshot.getValue(String::class.java)
                val nilaiPh = nilaiPhString?.toFloatOrNull()
                Log.d("FirebasePH", "pH dari Firebase: $nilaiPh")

                if (nilaiPh != null && nilaiPh in 0.0..14.0) {
                    txtPhValue.text = "pH Sekarang: %.2f".format(nilaiPh)
                    txtPhKosong.visibility = TextView.GONE

                    phEntries.add(Entry(indexCounter, nilaiPh))
                    indexCounter += 1f

                    if (phEntries.size > 20) {
                        phEntries.removeAt(0)
                        for (i in phEntries.indices) {
                            phEntries[i] = Entry(i.toFloat(), phEntries[i].y)
                        }
                        indexCounter = phEntries.size.toFloat()
                    }

                    val dataSet = LineDataSet(phEntries, "Riwayat pH").apply {
                        color = android.graphics.Color.CYAN
                        valueTextColor = android.graphics.Color.BLACK
                        lineWidth = 2f
                        setDrawCircles(true)
                        setDrawValues(false)
                    }

                    phChart.data = LineData(dataSet)
                    phChart.notifyDataSetChanged()
                    phChart.invalidate()
                } else {
                    txtPhKosong.visibility = TextView.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@PhActivity,
                    "Gagal membaca pH: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}