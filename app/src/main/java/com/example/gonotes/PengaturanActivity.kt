package com.example.gonotes

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PengaturanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengaturan)

        // Tombol kembali ke HomeActivity
        val btnBackToHome = findViewById<ImageView>(R.id.BackToHome)
        btnBackToHome.setOnClickListener {
            // Kembali ke HomeActivity
            finish() // Menutup halaman Pengaturan dan kembali ke halaman sebelumnya
        }
    }
}
