package com.example.gonotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter untuk RecyclerView yang digunakan untuk menampilkan daftar catatan
class NoteAdapter(
    private val notes: List<Note>, // Daftar catatan yang akan ditampilkan
    private val onEditClick: (Note) -> Unit, // Callback untuk aksi edit catatan
    private val onDeleteClick: (Note) -> Unit, // Callback untuk aksi hapus catatan
    private val onArchiveClick: (Note) -> Unit // Callback untuk aksi arsipkan catatan
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    // ViewHolder untuk mengelola tampilan tiap item dalam RecyclerView
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.noteTitle) // TextView untuk judul catatan
        val contentTextView: TextView = itemView.findViewById(R.id.noteContent) // TextView untuk isi catatan
        val menuButton: ImageButton = itemView.findViewById(R.id.menuButton) // Tombol menu (tiga titik)
    }

    // Dipanggil saat RecyclerView membutuhkan ViewHolder baru
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view) // Mengembalikan instance NoteViewHolder
    }

    // Dipanggil untuk menghubungkan data catatan dengan ViewHolder
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position] // Mendapatkan catatan berdasarkan posisi
        holder.titleTextView.text = note.title // Menampilkan judul catatan
        holder.contentTextView.text = note.content // Menampilkan isi catatan

        // Menangani klik tombol menu (tiga titik) pada tiap item
        holder.menuButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, view) // Membuat PopupMenu
            popupMenu.menuInflater.inflate(R.menu.menu_actions, popupMenu.menu) // Menghubungkan menu dengan file XML

            // Menangani klik pada item menu
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        onEditClick(note) // Callback untuk aksi edit
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(note) // Callback untuk aksi hapus
                        true
                    }
                    R.id.action_arsip -> {
                        onArchiveClick(note) // Callback untuk aksi arsip
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show() // Menampilkan menu popup
        }
    }

    // Mengembalikan jumlah total item dalam daftar catatan
    override fun getItemCount(): Int = notes.size
}
