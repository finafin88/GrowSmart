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
import com.example.growsmart.mobile.SensorData



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
fun SensorInputScreen(status: String = "") {
    var suhu by remember { mutableStateOf("") }
    var ph by remember { mutableStateOf("") }
    var tds by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("") }

    val ref = FirebaseDatabase.getInstance().getReference("GrowSmart/SensorData")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Input Data Sensor", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(value = suhu, onValueChange = { suhu = it }, label = { Text("Suhu (°C)") })
        OutlinedTextField(value = ph, onValueChange = { ph = it }, label = { Text("pH") })
        OutlinedTextField(value = tds, onValueChange = { tds = it }, label = { Text("TDS (ppm)") })

        Button(
            onClick = {
                val data = mapOf("suhu" to suhu, "ph" to ph, "tds" to tds)
                val timestamp = System.currentTimeMillis().toString()
                ref.child(timestamp).setValue(data)
                    .addOnSuccessListener {
                        statusText = "Data berhasil dikirim ke Firebase"
                        suhu = ""
                        ph = ""
                        tds = ""
                    }
                    .addOnFailureListener {
                        statusText = "Gagal mengirim data"
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Kirim ke Firebase")
        }

        if (statusText.isNotEmpty()) {
            Text(
                text = statusText,
                color = if (statusText.startsWith("Data berhasil")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

