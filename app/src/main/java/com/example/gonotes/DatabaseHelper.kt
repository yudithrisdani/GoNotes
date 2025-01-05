package com.example.gonotes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Nama database dan versi
        private const val DATABASE_NAME = "notes.db"
        private const val DATABASE_VERSION = 3  // Upgrade versi untuk tabel baru
        // Nama dan kolom untuk tabel catatan
        private const val TABLE_NAME = "notes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
        // Nama tabel untuk catatan yang dihapus dan diarsipkan
        private const val DELETED_TABLE_NAME = "deleted_notes"  // Tabel untuk sampah
        private const val ARCHIVED_TABLE_NAME = "archived_notes"  // Tabel untuk arsip

        // Tabel Pengguna untuk login
        const val USER_TABLE_NAME = "users"
        const val COLUMN_ID_USER = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
    }


    // Membuat tabel yang dibutuhkan di database
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_CONTENT TEXT
            )
        """

        val createDeletedNotesTable = """
            CREATE TABLE $DELETED_TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_CONTENT TEXT
            )
        """

        val createArchivedNotesTable = """
            CREATE TABLE $ARCHIVED_TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT,
                $COLUMN_CONTENT TEXT
            )
        """

        // Tabel untuk menyimpan data pengguna
        val createTableQuery = """
            CREATE TABLE $USER_TABLE_NAME (
                $COLUMN_ID_USER INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL
            )
        """

        // Eksekusi perintah untuk membuat tabel-tabel
        db?.execSQL(createTableQuery) //user
        db?.execSQL(createTable) //catatan
        db?.execSQL(createDeletedNotesTable)  // Membuat tabel sampah
        db?.execSQL(createArchivedNotesTable)  // Membuat tabel arsip
    }

    // Menangani perubahan versi database, memperbarui struktur tabel
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.execSQL("CREATE TABLE IF NOT EXISTS $DELETED_TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TITLE TEXT, $COLUMN_CONTENT TEXT)")
        }
        if (oldVersion < 3) { // Menambahkan upgrade untuk arsip
            db?.execSQL("CREATE TABLE IF NOT EXISTS $ARCHIVED_TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TITLE TEXT, $COLUMN_CONTENT TEXT)")
        }
    }

    fun insertUser(name: String, email: String, password: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, name)
        contentValues.put(COLUMN_EMAIL, email)
        contentValues.put(COLUMN_PASSWORD, password)

        val result = db.insert(USER_TABLE_NAME, null, contentValues)
        db.close()
        return result != -1L
    }

    fun isEmailRegistered(email: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $USER_TABLE_NAME WHERE $COLUMN_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val isRegistered = cursor.count > 0

        // Tambahkan log
        Log.d("DatabaseHelper", "Checking email: $email, isRegistered: $isRegistered")

        cursor.close()
        db.close()
        return isRegistered
    }


    fun validateUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $USER_TABLE_NAME WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val isValid = cursor.count > 0
        cursor.close()
        db.close()
        return isValid
    }

    // Menambahkan catatan baru
    fun addNote(note: Note): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    // Mendapatkan semua catatan
    fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                val content = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTENT))
                notes.add(Note(id, title, content))
            }
        }
        return notes
    }

    // Memperbarui catatan yang sudah ada
    fun updateNote(note: Note): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_TITLE, note.title)
            put(COLUMN_CONTENT, note.content)
        }

        // Update catatan di database berdasarkan ID
        val result = db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(note.id.toString()))
        db.close()

        return result > 0  // Returns true jika update berhasil
    }

    // Memindahkan catatan yang dihapus ke dalam tabel sampah
    fun deleteNoteToTrash(id: Int): Int {
        val db = writableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))

        var result = 0
        if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))

            // Memasukkan catatan ke tabel deleted_notes (sampah)
            val insertQuery = "INSERT INTO $DELETED_TABLE_NAME ($COLUMN_TITLE, $COLUMN_CONTENT) VALUES (?, ?)"
            db.execSQL(insertQuery, arrayOf(title, content))

            // Menghapus catatan dari tabel utama
            result = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        }
        cursor.close()
        return result
    }

    // Mengambil semua catatan yang ada di sampah
    fun getAllDeletedNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.query(DELETED_TABLE_NAME, null, null, null, null, null, null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                val content = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTENT))
                notes.add(Note(id, title, content))
            }
        }
        return notes
    }

    // Mengembalikan catatan dari sampah ke tabel utama
    fun restoreNoteFromTrash(id: Int): Int {
        val db = writableDatabase
        val query = "SELECT * FROM $DELETED_TABLE_NAME WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))

        var result = 0
        if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))

            // Memasukkan catatan kembali ke tabel utama
            val insertQuery = "INSERT INTO $TABLE_NAME ($COLUMN_TITLE, $COLUMN_CONTENT) VALUES (?, ?)"
            db.execSQL(insertQuery, arrayOf(title, content))

            // Menghapus catatan dari tabel sampah
            result = db.delete(DELETED_TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        }
        cursor.close()
        return result
    }

    // Menghapus catatan secara permanen dari tabel deleted_notes
    fun deleteNotePermanentlyFromTrash(id: Int): Int {
        val db = writableDatabase
        return db.delete(DELETED_TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    // Mengarsipkan catatan
    fun archiveNote(id: Int): Int {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            // Ambil catatan dari tabel utama
            val cursor = db.rawQuery("SELECT * FROM notes WHERE id = ?", arrayOf(id.toString()))
            if (cursor.moveToFirst()) {
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))

                // Masukkan ke tabel arsip
                val insertStatement = db.compileStatement("INSERT INTO archived_notes (title, content) VALUES (?, ?)")
                insertStatement.bindString(1, title)
                insertStatement.bindString(2, content)
                insertStatement.executeInsert()

                // Hapus dari tabel utama
                val deleteResult = db.delete("notes", "id = ?", arrayOf(id.toString()))
                if (deleteResult > 0) {
                    db.setTransactionSuccessful()
                    deleteResult
                } else {
                    0
                }
            } else {
                0
            }
        } finally {
            db.endTransaction()
        }
    }

    // Mengambil semua catatan yang ada di arsip
    fun getAllArchivedNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.query(ARCHIVED_TABLE_NAME, null, null, null, null, null, null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID))
                val title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE))
                val content = it.getString(it.getColumnIndexOrThrow(COLUMN_CONTENT))
                notes.add(Note(id, title, content))
            }
        }
        return notes
    }

    // Mengembalikan catatan dari arsip ke tabel utama
    fun restoreNoteFromArchive(id: Int): Int {
        val db = writableDatabase
        val query = "SELECT * FROM $ARCHIVED_TABLE_NAME WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))

        var result = 0
        if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))

            // Memasukkan catatan kembali ke tabel utama
            val insertQuery = "INSERT INTO $TABLE_NAME ($COLUMN_TITLE, $COLUMN_CONTENT) VALUES (?, ?)"
            db.execSQL(insertQuery, arrayOf(title, content))

            // Menghapus catatan dari tabel arsip
            result = db.delete(ARCHIVED_TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        }
        cursor.close()
        return result
    }

    // Menghapus catatan secara permanen dari arsip dan memindahkannya ke tabel deleted_notes
    fun deleteNotePermanentlyFromArchive(id: Int): Int {
        val db = writableDatabase
        db.beginTransaction()
        var result = 0
        try {
            // Ambil catatan dari arsip
            val cursor = db.rawQuery("SELECT * FROM $ARCHIVED_TABLE_NAME WHERE $COLUMN_ID = ?", arrayOf(id.toString()))
            if (cursor.moveToFirst()) {
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))

                // Masukkan catatan ke tabel deleted_notes
                val insertStatement = db.compileStatement("INSERT INTO $DELETED_TABLE_NAME (title, content) VALUES (?, ?)")
                insertStatement.bindString(1, title)
                insertStatement.bindString(2, content)
                insertStatement.executeInsert()

                // Hapus dari arsip
                result = db.delete(ARCHIVED_TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return result
    }
}
