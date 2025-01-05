package com.example.gonotes

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class GoNotesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gonotes)

        // Tombol kembali ke HomeActivity
        val btnBackToHome = findViewById<ImageView>(R.id.BackToHome)
        btnBackToHome.setOnClickListener {
            // Kembali ke HomeActivity
            finish() // Akan menutup halaman Versi dan kembali ke halaman sebelumnya (HomeActivity)
        }
    }
}
