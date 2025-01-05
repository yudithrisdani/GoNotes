package com.example.gonotes

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeActivity : AppCompatActivity() {

    private lateinit var noteDatabaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        noteDatabaseHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerView)
        val fabAdd = findViewById<ImageView>(R.id.fabAdd)
        val menuListIcon = findViewById<ImageView>(R.id.menuListIcon)
        val iconProfile = findViewById<ImageView>(R.id.iconProfile)

        recyclerView.layoutManager = LinearLayoutManager(this)
        loadNotes()

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditNoteActivity::class.java))
        }

        menuListIcon.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.menu_list, popupMenu.menu)

            // Listener untuk item yang diklik
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.action_gonotes -> {
                        // Menampilkan halaman VersiActivity ketika Pengaturan dipilih
                        val intent = Intent(this, GoNotesActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.action_pengingat -> Toast.makeText(this, "Pengingat dipilih", Toast.LENGTH_SHORT).show()
                    R.id.action_label -> Toast.makeText(this, "Label dipilih", Toast.LENGTH_SHORT).show()
                    R.id.action_arsip -> {
                        // Menampilkan arsip yang ada di sampah
                        val intent = Intent(this, ArsipActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.action_sampah -> {
                        // Menampilkan catatan yang ada di sampah
                        val intent = Intent(this, TrashActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.action_pengaturan -> {
                        // Menampilkan halaman VersiActivity ketika Pengaturan dipilih
                        val intent = Intent(this, PengaturanActivity::class.java)
                        startActivity(intent)
                    }
                }
                true
            }

            // Tampilkan menu
            popupMenu.show()
        }

        iconProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadNotes() {
        val notes = noteDatabaseHelper.getAllNotes()
        adapter = NoteAdapter(notes, { note ->
            // Edit Note
            val intent = Intent(this, AddEditNoteActivity::class.java)
            intent.putExtra("id", note.id)
            intent.putExtra("title", note.title)
            intent.putExtra("content", note.content)
            startActivity(intent)
        }, { note ->
            // Pindahkan Note ke Sampah
            noteDatabaseHelper.deleteNoteToTrash(note.id)
            Toast.makeText(this, "Catatan Dipindahkan ke Sampah", Toast.LENGTH_SHORT).show()
            loadNotes()  // Reload notes after moving to trash
        }, onArchiveClick = { note ->
            val result = noteDatabaseHelper.archiveNote(note.id)
            if (result > 0) {
                Toast.makeText(this, "Catatan berhasil diarsip", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal mengarsipkan catatan", Toast.LENGTH_SHORT).show()
            }
            loadNotes()
        })
        recyclerView.adapter = adapter
    }
    // Saat aplikasi berada pada recent button
    override fun onResume() {
        super.onResume()
        loadNotes()
    }
}



