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
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DatabaseReference


class NutrisiActivity : BaseActivity() {

    private lateinit var nutrisiChart: LineChart
    private lateinit var txtNutrisiValue: TextView
    private lateinit var btnNutrisiAB: Button

    private lateinit var refLogNutrisi: DatabaseReference
    private lateinit var refKontrolNutrisi: DatabaseReference

    private val nutrisiEntries = ArrayList<Entry>()
    private var indexCounter = 0f

    override fun getLayoutResourceId(): Int = R.layout.activity_nutrisi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Nutrisi AB")

        nutrisiChart = findViewById(R.id.nutrisiChart)
        txtNutrisiValue = findViewById(R.id.txtNutrisiValue)
        btnNutrisiAB = findViewById(R.id.btnNutrisiAB)

        refLogNutrisi = FirebaseDatabase.getInstance().getReference("GrowSmart/log/nutrisi")
        refKontrolNutrisi = FirebaseDatabase.getInstance().getReference("manual/nutrisi_ab")

        refLogNutrisi.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                nutrisiEntries.clear()
                indexCounter = 0f

                val sortedData = snapshot.children.sortedBy { it.key?.toLongOrNull() }

                for (data in sortedData) {
                    val valueStr = data.getValue(String::class.java)
                    val value = valueStr?.toFloatOrNull() ?: continue

                    nutrisiEntries.add(Entry(indexCounter++, value))
                }

                if (nutrisiEntries.isNotEmpty()) {
                    txtNutrisiValue.text = "${nutrisiEntries.last().y} ppm"
                }

                val dataSet = LineDataSet(nutrisiEntries, "Nutrisi AB (ppm)").apply {
                    color = Color.BLUE
                    valueTextColor = Color.BLACK
                    lineWidth = 2f
                    circleRadius = 4f
                    setCircleColor(Color.BLUE)
                    setDrawValues(false)
                }

                nutrisiChart.data = LineData(dataSet)
                nutrisiChart.description.text = "Grafik Nutrisi AB"
                nutrisiChart.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NutrisiActivity, "Gagal mengambil data nutrisi", Toast.LENGTH_SHORT).show()
            }
        })

        // Ambil status kontrol nutrisi
        refKontrolNutrisi.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOn = snapshot.getValue(Boolean::class.java) ?: false
                btnNutrisiAB.text = if (isOn) "Matikan Nutrisi AB" else "Nyalakan Nutrisi AB"
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        btnNutrisiAB.setOnClickListener {
            refKontrolNutrisi.get().addOnSuccessListener { snapshot ->
                val currentState = snapshot.getValue(Boolean::class.java) ?: false
                refKontrolNutrisi.setValue(!currentState)
                showToastNutrisi(!currentState)
            }
        }
    }

    private fun showToastNutrisi(nyala: Boolean) {
        val layout = layoutInflater.inflate(R.layout.toast_status, findViewById(android.R.id.content), false)

        val icon = layout.findViewById<ImageView>(R.id.toast_icon)
        val text = layout.findViewById<TextView>(R.id.toast_text)

        if (nyala) {
            icon.setImageResource(R.drawable.ic_check_circle)
            text.text = "Nutrisi AB berhasil DINYALAKAN"
        } else {
            icon.setImageResource(R.drawable.ic_error)
            text.text = "Nutrisi AB berhasil DIMATIKAN"
        }

        val toast = Toast(applicationContext)
        toast.view = layout
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.show()
    }
}