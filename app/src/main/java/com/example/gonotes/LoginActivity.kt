package com.example.gonotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gonotes.DatabaseHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        databaseHelper = DatabaseHelper(this)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerTextView = findViewById<TextView>(R.id.registerTextView)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua bidang wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi login
            val isValidUser = databaseHelper.validateUser(email, password)
            if (isValidUser) {
                Toast.makeText(this, "Login Sukses", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)

                //untuk profile
                val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("EMAIL", email)
                    apply()
                }
            } else {
                Toast.makeText(this, "Email atau kata sandi tidak valid", Toast.LENGTH_SHORT).show()
            }
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}

