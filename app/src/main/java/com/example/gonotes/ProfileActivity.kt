package com.example.gonotes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val nameTextView: TextView = findViewById(R.id.nameTextView)
        val userEmailTextView: TextView = findViewById(R.id.userEmailTextView)
        val backButtonHome: Button = findViewById(R.id.backButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        val sharedPref = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
        val email = sharedPref.getString("EMAIL", null)

        if (email != null) {
            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.readableDatabase
            val query = """
                SELECT ${DatabaseHelper.COLUMN_NAME}, ${DatabaseHelper.COLUMN_EMAIL}
                FROM ${DatabaseHelper.USER_TABLE_NAME} 
                WHERE ${DatabaseHelper.COLUMN_EMAIL} = ?
            """
            val cursor = db.rawQuery(query, arrayOf(email))

            if (cursor.moveToFirst()) {
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME))
                val fetchedEmail =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL))

                nameTextView.text = name
                userEmailTextView.text = fetchedEmail
            }
            cursor.close()
            db.close()
        }

        backButtonHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        }

        logoutButton.setOnClickListener {
            sharedPref.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
