package com.growsmart.mobile

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase

class KatupActivity : BaseActivity() {

    private lateinit var btnPompa: Button
    private var isPompaOn = false

    private lateinit var btnKatup: Button
    private var katupAktif = false

    private lateinit var btnKatupKeluar: Button
    private var katupKeluarAktif = false


    override fun getLayoutResourceId(): Int = R.layout.activity_katup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnPompa = findViewById(R.id.btnPompa)


        btnPompa.setOnClickListener {
            isPompaOn = !isPompaOn
            updatePompaButton()
            showToastPompa(isPompaOn)

            // (Opsional) kirim ke Firebase
            val pompaRef = FirebaseDatabase.getInstance().getReference("kontrol_manual/pompa")
            pompaRef.setValue(isPompaOn)
        }
        btnKatup = findViewById(R.id.btnKatup)

        btnKatup.setOnClickListener {
            katupAktif = !katupAktif
            if (katupAktif) {
                btnKatup.text = "Katup ON"
                btnKatup.setBackgroundResource(R.drawable.bg_button_on)
                Toast.makeText(this, "Katup berhasil DIBUKA", Toast.LENGTH_SHORT).show()
            } else {
                btnKatup.text = "Katup OFF"
                btnKatup.setBackgroundResource(R.drawable.bg_button_off)
                Toast.makeText(this, "Katup berhasil DITUTUP", Toast.LENGTH_SHORT).show()
            }
        }

        btnKatupKeluar = findViewById(R.id.btnKatupKeluar)

        btnKatupKeluar.setOnClickListener {
            katupKeluarAktif = !katupKeluarAktif
            btnKatupKeluar.text = if (katupKeluarAktif) "Katup Keluar ON" else "Katup Keluar OFF"
            btnKatupKeluar.setBackgroundResource(
                if (katupKeluarAktif) R.drawable.bg_button_on else R.drawable.bg_button_off
            )

            Toast.makeText(
                this,
                if (katupKeluarAktif) "Katup Keluar berhasil DIBUKA" else "Katup Keluar berhasil DITUTUP",
                Toast.LENGTH_SHORT
            ).show()
        }


    }

    private fun updatePompaButton() {
        if (isPompaOn) {
            btnPompa.text = "Pompa ON"
            btnPompa.setBackgroundResource(R.drawable.bg_button_on)
        } else {
            btnPompa.text = "Pompa OFF"
            btnPompa.setBackgroundResource(R.drawable.bg_button_off)
        }
    }

    private fun showToastPompa(aktif: Boolean) {
        val layout = layoutInflater.inflate(R.layout.toast_status, null)
        val icon = layout.findViewById<ImageView>(R.id.toast_icon)
        val text = layout.findViewById<TextView>(R.id.toast_text)

        if (aktif) {
            icon.setImageResource(R.drawable.ic_check_circle)
            text.text = "Pompa berhasil DINYALAKAN"
        } else {
            icon.setImageResource(R.drawable.ic_error)
            text.text = "Pompa berhasil DIMATIKAN"
        }

        val toast = Toast(applicationContext)
        toast.view = layout
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.show()
    }
}
