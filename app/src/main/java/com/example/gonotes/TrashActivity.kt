package com.example.gonotes

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TrashActivity : AppCompatActivity() {

    private lateinit var noteDatabaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        noteDatabaseHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadDeletedNotes()

        // Inisialisasi tombol dan tambahkan klik listener
        val backToHomeButton = findViewById<ImageView>(R.id.BackToHome)
        backToHomeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Menutup ArsipActivity agar tidak kembali ke halaman ini saat menekan tombol kembali
        }
    }


    private fun loadDeletedNotes() {
        val deletedNotes = noteDatabaseHelper.getAllDeletedNotes()
        adapter = NoteAdapter(
            deletedNotes,
            onEditClick = { note ->
                // Memulihkan catatan dari sampah ke daftar utama
                val result = noteDatabaseHelper.restoreNoteFromTrash(note.id)
                if (result > 0) {
                    Toast.makeText(this, "Catatan Dikembalikan ke Daftar Utama Untuk Diedit", Toast.LENGTH_SHORT).show()
                    loadDeletedNotes() // Refresh data
                } else {
                    Toast.makeText(this, "Gagal mengedit catatan", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { note ->
                // Hapus catatan secara permanen dari sampah
                val result = noteDatabaseHelper.deleteNotePermanentlyFromTrash(note.id)
                if (result > 0) {
                    Toast.makeText(this, "Catatan Dihapus Permanen", Toast.LENGTH_SHORT).show()
                    loadDeletedNotes() // Refresh data setelah dihapus
                } else {
                    Toast.makeText(this, "Gagal menghapus catatan ", Toast.LENGTH_SHORT).show()
                }
            },
            onArchiveClick = { note ->
                // Menampilkan pesan bahwa catatan yang sudah dihapus tidak bisa diarsipkan
                Toast.makeText(this, "Catatan Yang Sudah Dihapus Tidak Bisa Diarsipkan", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter
    }

}
