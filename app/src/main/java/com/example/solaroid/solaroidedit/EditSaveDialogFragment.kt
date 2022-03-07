package com.example.solaroid.solaroidedit

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.lang.ClassCastException
import java.lang.Exception
import java.lang.IllegalStateException

class EditSaveDialogFragment(_listener: EditSaveDialogListener) : DialogFragment() {
    internal var listener: EditSaveDialogListener = _listener

    interface EditSaveDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireParentFragment().let {
            val builder = AlertDialog.Builder(it.context)
            builder?.setMessage("저장하시겠습니까?")
                .setPositiveButton("저장", DialogInterface.OnClickListener { dialogInterface, i ->
                    listener.onDialogPositiveClick(this)
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->
                    listener.onDialogNegativeClick(this)
                })
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}