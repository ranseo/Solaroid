package com.example.solaroid.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class ChoiceDialogFragment(_listener: ChoiceDialogListener) : DialogFragment() {
    internal var listener: ChoiceDialogListener = _listener

    interface ChoiceDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireParentFragment().let {
            val builder = AlertDialog.Builder(it.context)
            builder?.setMessage("해당 사진을 선택 하시겠습니까?")
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