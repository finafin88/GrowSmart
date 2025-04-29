package com.growsmart.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import com.growsmart.mobile.SensorData

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SensorInputScreen()
                }
            }
        }
    }
}

@Composable
fun SensorInputScreen() {
    var suhu by remember { mutableStateOf("") }
    var ph by remember { mutableStateOf("") }
    var tds by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    val ref = FirebaseDatabase.getInstance().getReference("GrowSmart/SensorData")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = suhu,
            onValueChange = { suhu = it },
            label = { Text("Suhu (°C)") }
        )
        OutlinedTextField(
            value = ph,
            onValueChange = { ph = it },
            label = { Text("pH") }
        )
        OutlinedTextField(
            value = tds,
            onValueChange = { tds = it },
            label = { Text("TDS (ppm)") }
        )
        Button(onClick = {
            val data = SensorData(
                suhu = suhu.toDoubleOrNull() ?: 0.0,
                ph = ph.toDoubleOrNull() ?: 0.0,
                tds = tds.toDoubleOrNull() ?: 0.0
            )
            val timestamp = System.currentTimeMillis().toString()
            ref.child(timestamp).setValue(data)
                .addOnSuccessListener { status = "✅ Data berhasil dikirim ke Firebase" }
                .addOnFailureListener { status = "❌ Gagal mengirim data" }
        }) {
            Text("Kirim ke Firebase")
        }

        if (status.isNotEmpty()) {
            Text(text = status)
        }
    }
}
