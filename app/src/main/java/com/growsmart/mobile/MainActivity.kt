package com.growsmart.mobile

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class MainActivity : BaseActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var txtPh: TextView
    private lateinit var txtTds: TextView
    private lateinit var txtSuhu: TextView

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("LAYOUT_TEST", "Layout berhasil dimuat")

        txtPh = findViewById(R.id.txtPh)
        txtTds = findViewById(R.id.txtTds)
        txtSuhu = findViewById(R.id.txtSuhu)

        database = FirebaseDatabase.getInstance().reference

        ambilDataSensor()
    }



    private fun ambilDataSensor() {
        val sensorRef = database.child("sensor")

        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FIREBASE_CHECK", "Snapshot received: ${snapshot.value}")

                val ph = snapshot.child("ph").getValue(String::class.java)
                val tds = snapshot.child("tds").getValue(String::class.java)
                val suhu = snapshot.child("suhu").getValue(String::class.java)

                txtPh.text = "pH: ${ph ?: "--"}"
                txtTds.text = "TDS: ${tds ?: "--"} ppm"
                txtSuhu.text = "Suhu: ${suhu ?: "--"} °C"

                Log.d("FirebaseData", "ph=$ph, tds=$tds, suhu=$suhu")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseData", "Gagal ambil data: ${error.message}")
            }
        })
    }
}
