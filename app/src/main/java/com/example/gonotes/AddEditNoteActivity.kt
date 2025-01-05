package com.example.gonotes

// Import paket dan kelas yang dibutuhkan untuk implementasi Activity, manipulasi data, dan UI elemen.
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * AddEditNoteActivity adalah aktivitas yang bertanggung jawab untuk menambah atau mengedit catatan.
 * Kegiatan ini berinteraksi dengan SQLite melalui DatabaseHelper.
 */
class AddEditNoteActivity : AppCompatActivity() {

    // DatabaseHelper digunakan untuk melakukan operasi CRUD pada database.
    private lateinit var noteDatabaseHelper: DatabaseHelper

    // Variabel untuk menyimpan ID catatan yang sedang diedit. Nilai awalnya null jika menambah catatan baru.
    private var noteId: Int? = null

    /**
     * onCreate adalah metode utama yang dipanggil saat aktivitas ini dibuat.
     * Bertanggung jawab untuk menginisialisasi elemen UI dan memproses intent dari aktivitas sebelumnya.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        // Inisialisasi instance DatabaseHelper untuk berkomunikasi dengan SQLite.
        noteDatabaseHelper = DatabaseHelper(this)

        // Menghubungkan elemen UI dari XML dengan variabel di Kotlin.
        val editTitle = findViewById<EditText>(R.id.editTitle) // Input untuk judul catatan.
        val editContent = findViewById<EditText>(R.id.editContent) // Input untuk isi catatan.
        val btnSave = findViewById<Button>(R.id.btnSave) // Tombol untuk menyimpan catatan.

        // Mengambil data dari intent untuk mengatur mode edit atau tambah.
        noteId = intent.getIntExtra("id", -1).takeIf { it != -1 }
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")

        // Jika data tersedia, isi field dengan nilai awal (untuk mode edit).
        editTitle.setText(title)
        editContent.setText(content)

        // Menangani aksi tombol kembali ke HomeActivity.
        val backToHomeButton = findViewById<ImageView>(R.id.BackToHome)
        backToHomeButton.setOnClickListener {
            // Intent berpindah ke HomeActivity.
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Mengakhiri aktivitas ini.
        }

        // Menangani aksi tombol simpan.
        btnSave.setOnClickListener {
            val newTitle = editTitle.text.toString() // Mendapatkan teks dari input judul.
            val newContent = editContent.text.toString() // Mendapatkan teks dari input isi.

            // Validasi: Tidak boleh ada bidang yang kosong.
            if (newTitle.isEmpty() || newContent.isEmpty()) {
                Toast.makeText(this, "Bidang tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Jika tidak ada ID, tambahkan catatan baru.
            if (noteId == null) {
                noteDatabaseHelper.addNote(Note(title = newTitle, content = newContent))
                Toast.makeText(this, "Catatan Ditambahkan", Toast.LENGTH_SHORT).show()
            } else {
                // Jika ada ID, perbarui catatan yang ada.
                noteDatabaseHelper.updateNote(Note(noteId!!, newTitle, newContent))
                Toast.makeText(this, "Catatan Diperbarui", Toast.LENGTH_SHORT).show()
            }
            finish() // Mengakhiri aktivitas ini setelah penyimpanan berhasil.
        }
    }
}
