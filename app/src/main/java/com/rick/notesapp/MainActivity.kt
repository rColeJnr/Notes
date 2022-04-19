package com.rick.notesapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.rick.notesapp.databinding.ActivityMainBinding
import layout.NoteAdapter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener {
            NewNote().show(supportFragmentManager, "")
        }

        noteAdapter = NoteAdapter(this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            itemAnimator = DefaultItemAnimator()
            adapter = noteAdapter
        }

        noteAdapter.noteList = retrieveNotes()
        noteAdapter.notifyItemRangeInserted(0, noteAdapter.noteList.size)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onStart() {
        super.onStart()
        val nightTheme = sharedPreferences.getBoolean("theme", false)
        if (nightTheme) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val showDividingLines = sharedPreferences.getBoolean("dividingLines", false)
        if (showDividingLines) binding.recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        else if (binding.recyclerView.itemDecorationCount > 0) binding.recyclerView.removeItemDecorationAt(0)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun createNewNote(note: Note) {
        noteAdapter.noteList.add(note)
        noteAdapter.notifyItemInserted(noteAdapter.noteList.size -1)
        saveNotes()
    }

    private fun saveNotes() {
        val notes = noteAdapter.noteList
        val gson = GsonBuilder().create()
        val jsonNote = gson.toJson(notes)

        var writer: Writer? = null
        try {
//            In this instance, the operating
//mode is set to private, which means the notes.json file will only be accessible to this application.
            val out = this.openFileOutput(FILEPATH, Context.MODE_PRIVATE)

            writer = OutputStreamWriter(out)
            writer.write(jsonNote)
        } catch (e: Exception){
            writer?.close()
        } finally {
            writer?.close()
        }
    }

    private fun retrieveNotes(): MutableList<Note>{
        var noteList = mutableListOf<Note>()
        if (this.getFileStreamPath(FILEPATH).isFile) {
            var reader: BufferedReader? = null
            try {
                val fileInput = this.openFileInput(FILEPATH)
                reader = BufferedReader(InputStreamReader(fileInput))
                val stringBuilder = StringBuilder()

                for (line in reader.readLine()) stringBuilder.append(line)

                if (stringBuilder.isNotEmpty()){
                    val listType = object : TypeToken<List<Note>>() {}.type
                    noteList = Gson().fromJson(stringBuilder.toString(), listType)
                }
            } catch (e: Exception){
                reader?.close()
            } finally {
                reader?.close()
            }
        }
        return noteList
    }

    fun deleteNote(index: Int) {
        noteAdapter.noteList.removeAt(index)
        noteAdapter.notifyItemRemoved(index)
        saveNotes()
    }

    fun showNote(layoutPosition: Int) {
        val dialog = ShowNote(noteAdapter.noteList[layoutPosition], layoutPosition)
        dialog.show(supportFragmentManager, "")
    }

//    Companion objects are initialised when the outer class (MainActivity in this instance) loads.
    companion object {
        private const val FILEPATH = "notes.json"
    }
}