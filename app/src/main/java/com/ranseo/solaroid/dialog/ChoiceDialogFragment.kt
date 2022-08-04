package com.ranseo.solaroid.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class ChoiceDialogFragment(_listener: ChoiceDialogListener, uri: String) : DialogFragment() {
    internal var listener: ChoiceDialogListener = _listener
    var uri : String = uri

    interface ChoiceDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, uri:String)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireParentFragment().let {
            val builder = AlertDialog.Builder(it.context)
            builder?.setMessage("해당 사진을 선택 하시겠습니까?")
                .setPositiveButton("선택", DialogInterface.OnClickListener { dialogInterface, i ->
                    listener.onDialogPositiveClick(this, uri)
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->
                    listener.onDialogNegativeClick(this)
                })
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}