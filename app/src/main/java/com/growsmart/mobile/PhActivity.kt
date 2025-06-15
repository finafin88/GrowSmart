package com.growsmart.mobile

import android.os.Bundle
import android.view.Gravity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.FirebaseDatabase
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

class PhActivity : BaseActivity() {

    private lateinit var phChart: LineChart
    private lateinit var txtPhValue: TextView
    private lateinit var btnPhUp: Button
    private lateinit var btnPhDown: Button

    private lateinit var sensorRef: DatabaseReference
    private lateinit var logPhRef: DatabaseReference
    private lateinit var controlRef: DatabaseReference

    private var isPhUpOn = false
    private var isPhDownOn = false
    private val phEntries = ArrayList<Entry>()

    override fun getLayoutResourceId(): Int = R.layout.activity_ph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbarTitle("Grafik pH")

        phChart = findViewById(R.id.phChart)
        txtPhValue = findViewById(R.id.txtPhValue)
        btnPhUp = findViewById(R.id.btnPhUp)
        btnPhDown = findViewById(R.id.btnPhDown)

        val database = FirebaseDatabase.getInstance()
        val sensorRef = database.getReference("GrowSmart/sensor/ph")
        logPhRef = database.getReference("GrowSmart/log/ph")
        controlRef = database.getReference("GrowSmart/status/manual/ph")


        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                value?.let {
                   txtPhValue.text = "Nilai pH = $it"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                txtPhValue.text = "Gagal ambil data"
            }
        })

        logPhRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                phEntries.clear()
                var index = 0f
                for (data in snapshot.children) {
                    val value = data.getValue<String>()?.toFloatOrNull()
                    if (value != null) {
                        phEntries.add(Entry(index++, value))
                    }
                }
                val dataSet = LineDataSet(phEntries, "Log pH Air")
                dataSet.lineWidth = 2f
                dataSet.setDrawValues(false)
                dataSet.setDrawCircles(true)

                val lineData = LineData(dataSet)
                phChart.data = lineData

                val yAxis = phChart.axisLeft
                yAxis.axisMinimum = -0f
                yAxis.axisMaximum = 14f
                phChart.axisRight.isEnabled = false

                phChart.description.text = "Grafik pH"
                phChart.invalidate()

                phChart.data = LineData(dataSet)
                phChart.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Gagal ambil grafik pH", Toast.LENGTH_SHORT).show()
            }
        })


        btnPhUp.setOnClickListener {
            isPhUpOn = !isPhUpOn
            updatePhUpButton()
            showToastPh(isPhUpOn, true)
            controlRef.child("ph_up").setValue(isPhUpOn)
        }

        
        btnPhDown.setOnClickListener {
            isPhDownOn = !isPhDownOn
            updatePhDownButton()
            showToastPh(isPhDownOn, false)
            controlRef.child("ph_down").setValue(isPhDownOn)
        }

        updatePhUpButton()
        updatePhDownButton()
    }

    private fun updatePhUpButton() {
        if (isPhUpOn) {
            btnPhUp.text = "PH Up ON"
            btnPhUp.setBackgroundResource(R.drawable.bg_button_on)
        } else {
            btnPhUp.text = "PH Up OFF"
            btnPhUp.setBackgroundResource(R.drawable.bg_button_off)
        }
    }

    private fun updatePhDownButton() {
        if (isPhDownOn) {
            btnPhDown.text = "PH Down ON"
            btnPhDown.setBackgroundResource(R.drawable.bg_button_on)
        } else {
            btnPhDown.text = "PH Down OFF"
            btnPhDown.setBackgroundResource(R.drawable.bg_button_off)
        }
    }

    private fun showToastPh(aktif: Boolean, isPhUp: Boolean) {
        val layout = layoutInflater.inflate(R.layout.toast_status, null)
        val icon = layout.findViewById<ImageView>(R.id.toast_icon)
        val text = layout.findViewById<TextView>(R.id.toast_text)

        if (aktif) {
            icon.setImageResource(R.drawable.ic_check_circle)
            text.text = if (isPhUp) "PH Up berhasil DINYALAKAN" else "PH Down berhasil DINYALAKAN"
        } else {
            icon.setImageResource(R.drawable.ic_error)
            text.text = if (isPhUp) "PH Up berhasil DIMATIKAN" else "PH Down berhasil DIMATIKAN"
        }

        val toast = Toast(applicationContext)
        toast.view = layout
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.show()
    }
}