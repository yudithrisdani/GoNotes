package com.example.gonotes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        databaseHelper = DatabaseHelper(this)

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val rePasswordText = findViewById<EditText>(R.id.rePassword)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val footerTextView = findViewById<TextView>(R.id.footerTextView)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val rePassword = rePasswordText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(this, "Semua bidang wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != rePassword) {
                Toast.makeText(this, "Kata sandi tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (databaseHelper.isEmailRegistered(email)) {
                Toast.makeText(this, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isInserted = databaseHelper.insertUser(name, email, password)
            if (isInserted) {
                Log.d("RegisterActivity", "Berhasil membuat akun")
                Toast.makeText(this, "Berhasil membuat akun", Toast.LENGTH_SHORT).show()
                nameEditText.postDelayed({
                    finish()
                }, 1000)
            } else {
                Log.d("RegisterActivity", "Gagal membuat akun")
                Toast.makeText(this, "Gagal membuat akun", Toast.LENGTH_SHORT).show()
            }

        }

        footerTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
