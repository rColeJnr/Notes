package com.rick.notesapp

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.rick.notesapp.databinding.ShowNoteBinding

class ShowNote(private val note: Note, private val index: Int): DialogFragment() {

    private var _binding: ShowNoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val callingActivity = activity as MainActivity
        val inflater = callingActivity.layoutInflater
        _binding = ShowNoteBinding.inflate(inflater)

        val builder = AlertDialog.Builder(callingActivity)
            .setView(binding.root)

        binding.apply {
            txtTitle.text = note.title
            txtContents.text = note.contents
            btnOk.setOnClickListener { dismiss() }
            btnDelete.setOnClickListener {
                callingActivity.deleteNote(index)
                Toast.makeText(callingActivity, resources.getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}