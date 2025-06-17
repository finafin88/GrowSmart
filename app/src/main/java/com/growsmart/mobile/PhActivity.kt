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
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhActivity : BaseActivity() {

    private lateinit var phChart: LineChart
    private lateinit var txtPhValue: TextView
    private lateinit var btnPhUp: Button
    private lateinit var btnPhDown: Button


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
        logPhRef = database.getReference("GrowSmart/log/ph")
        controlRef = database.getReference("GrowSmart/status/manual/ph")


        logPhRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                phEntries.clear()

                val sorted = snapshot.children.sortedBy { it.key?.toLongOrNull() }

                for (data in sorted) {
                    val x = data.key?.toFloatOrNull() ?: continue
                    val y = data.getValue(String::class.java)?.toFloatOrNull() ?: continue
                    phEntries.add(Entry(x, y))
                }

                if (phEntries.isNotEmpty()) {
                    val latestPh = phEntries.last().y
                    txtPhValue.text = "Nilai pH = %.2f".format(latestPh)

                    val dataSet = LineDataSet(phEntries, "Log pH Air").apply {
                        lineWidth = 2f
                        setDrawValues(false)
                        setDrawCircles(true)
                    }

                    val lineData = LineData(dataSet)
                    phChart.data = lineData

                    phChart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val millis = value.toLong() * 1000
                            val date = Date(millis)
                            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                            return format.format(date)
                        }
                    }

                    val yAxis = phChart.axisLeft
                    yAxis.axisMinimum = 0f
                    yAxis.axisMaximum = 14f
                    phChart.axisRight.isEnabled = false

                    phChart.description.text = "Grafik pH"
                    phChart.invalidate()
                } else {
                    txtPhValue.text = "Data pH tidak tersedia"
                    phChart.clear()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                txtPhValue.text = "Gagal ambil data"
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