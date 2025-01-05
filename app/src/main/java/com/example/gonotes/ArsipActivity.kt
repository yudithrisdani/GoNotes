package com.example.gonotes

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ArsipActivity : AppCompatActivity() {

    private lateinit var noteDatabaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arsip)

        noteDatabaseHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewArchived)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadArchivedNotes()

        // Tombol kembali ke HomeActivity
        val backToHomeButton = findViewById<ImageView>(R.id.BackToHome)
        backToHomeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadArchivedNotes() {
        val archivedNotes = noteDatabaseHelper.getAllArchivedNotes()
        adapter = NoteAdapter(archivedNotes,
            onEditClick = { note ->
                // Fungsi untuk mengembalikan catatan dari arsip ke daftar utama
                noteDatabaseHelper.restoreNoteFromArchive(note.id)
                Toast.makeText(this, "Catatan Dikembalikan ke Daftar Utama Untuk Diedit", Toast.LENGTH_SHORT).show()
                loadArchivedNotes() // Refresh data
            },
            onDeleteClick = { note ->
                // Fungsi Delete Permanen dari Arsip, pindahkan ke sampah terlebih dahulu
                val result = noteDatabaseHelper.deleteNotePermanentlyFromArchive(note.id)
                if (result > 0) {
                    Toast.makeText(this, "Catatan Dihapus dari Arsip", Toast.LENGTH_SHORT).show()
                    loadArchivedNotes()  // Memuat ulang data arsip
                } else {
                    Toast.makeText(this, "Gagal menghapus catatan", Toast.LENGTH_SHORT).show()
                }
            },
            onArchiveClick = { note ->
                // Menampilkan pesan bahwa catatan yang sudah dihapus tidak bisa diarsipkan
                Toast.makeText(this, "Catatan Sudah Berada Diarsip", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadArchivedNotes()  // Reload notes whenever the activity is resumed
    }
}
