package com.ranseo.solaroid.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.ranseo.solaroid.R

class RenameDialog(
    _listener: RenameDialogListener, val titleMsg: String,
    val positiveMsg: String, val negativeMsg: String, val originalName: String
) : DialogFragment() {
    internal var listener: RenameDialogListener = _listener

    interface RenameDialogListener {
        fun onRenamePositive(dialog: DialogFragment, new:String)
        fun onRenameNegatvie(dialog: DialogFragment)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireParentFragment().let {

            val renameLayout = layoutInflater.inflate(R.layout.dialog_fragment_rename, null)
            val editText = renameLayout.findViewById<EditText>(R.id.et_rename)
            editText.setText(originalName)
            val builder = AlertDialog.Builder(it.context)
            builder.setMessage(titleMsg)
                .setView(renameLayout)
                .setPositiveButton(positiveMsg) { _, _ ->
                    listener.onRenamePositive(this, editText.text.toString())
                }
                .setNegativeButton(negativeMsg) { _, _ ->
                    listener.onRenameNegatvie(this)
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}