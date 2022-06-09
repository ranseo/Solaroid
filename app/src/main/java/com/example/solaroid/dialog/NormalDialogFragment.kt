package com.example.solaroid.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class NormalDialogFragment(_listener: NormalDialogListener, val titleMsg:String, val positiveMsg: String, val negativeMsg:String) : DialogFragment() {
    internal var listener: NormalDialogListener = _listener

    interface NormalDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireParentFragment().let {
            val builder = AlertDialog.Builder(it.context)
            builder?.setMessage(titleMsg)
                .setPositiveButton(positiveMsg, DialogInterface.OnClickListener { dialogInterface, i ->
                    listener.onDialogPositiveClick(this)
                })
                .setNegativeButton(negativeMsg, DialogInterface.OnClickListener { dialogInterface, i ->
                    listener.onDialogNegativeClick(this)
                })
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}