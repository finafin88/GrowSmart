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
import com.google.firebase.database.DatabaseReference
import android.widget.TextView



class NutrisiActivity : BaseActivity() {

    private lateinit var nutrisiChart: LineChart
    private lateinit var btnNutrisiAB: Button

    private lateinit var database: FirebaseDatabase
    private lateinit var tdsRef: DatabaseReference
    private lateinit var controlRef: DatabaseReference
    private lateinit var txtNutrisiValue: TextView

    private val tdsEntries = ArrayList<Entry>()
    private var indexCounter = 0f

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_nutrisi
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Nutrisi AB")

        nutrisiChart = findViewById(R.id.nutrisiChart)
        btnNutrisiAB = findViewById(R.id.btnNutrisiAB)

        database = FirebaseDatabase.getInstance()
        tdsRef = database.getReference("sensor/tds")
        controlRef = database.getReference("manual/nutrisi_ab")

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

        txtNutrisiValue = findViewById(R.id.txtNutrisiValue)

        tdsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tdsString = snapshot.getValue(String::class.java)
                val tdsValue = tdsString?.toFloatOrNull()

                if (tdsValue != null) {
                    txtNutrisiValue.text = "Nutrisi: %.1f ppm".format(tdsValue)

                    val entry = Entry(indexCounter, tdsValue)
                    lineData.addEntry(entry, 0)
                    indexCounter++

                    lineData.notifyDataChanged()
                    nutrisiChart.notifyDataSetChanged()
                    nutrisiChart.invalidate()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NutrisiActivity, "Gagal baca data TDS", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}