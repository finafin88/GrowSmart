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
    private lateinit var txtTdsValue: TextView
    private lateinit var btnNutrisiAB: Button

    private lateinit var refLogTds: DatabaseReference
    private lateinit var refNutrisiAB: DatabaseReference

    private val tdsEntries = ArrayList<Entry>()
    private var indexCounter = 0f

    override fun getLayoutResourceId(): Int = R.layout.activity_nutrisi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Nutrisi AB")

        nutrisiChart = findViewById(R.id.nutrisiChart)
        txtTdsValue = findViewById(R.id.txtTdsValue)
        btnNutrisiAB = findViewById(R.id.btnNutrisiAB)


        refLogTds = FirebaseDatabase.getInstance().getReference("GrowSmart/log/tds")
        refNutrisiAB = FirebaseDatabase.getInstance().getReference("manual/nutrisi_ab")


        refLogTds.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tdsEntries.clear()
                indexCounter = 0f

                val sorted = snapshot.children.sortedBy { it.key?.toLongOrNull() }
                for (data in sorted) {
                    val valueStr = data.getValue(String::class.java)
                    val value = valueStr?.toFloatOrNull() ?: continue
                    tdsEntries.add(Entry(indexCounter++, value))
                }

                if (tdsEntries.isNotEmpty()) {
                    txtTdsValue.text = "${tdsEntries.last().y} ppm"
                    val dataSet = LineDataSet(tdsEntries, "Nutrisi AB (ppm)").apply {
                        color = Color.BLUE
                        valueTextColor = Color.BLACK
                        lineWidth = 2f
                        circleRadius = 4f
                        setCircleColor(Color.BLUE)
                        setDrawValues(false)
                    }

                    val lineData = LineData(dataSet)
                    nutrisiChart.data = lineData
                    nutrisiChart.description.text = "Grafik Nutrisi AB"
                    nutrisiChart.invalidate()
                } else {
                    txtTdsValue.text = "Data tidak tersedia"
                    nutrisiChart.clear()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NutrisiActivity, "Gagal ambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })


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

        Toast(this).apply {
            view = layout
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            show()
        }
    }
}