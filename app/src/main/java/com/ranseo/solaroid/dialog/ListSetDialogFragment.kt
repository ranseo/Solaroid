package com.ranseo.solaroid.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ListSetDialogFragment(val itemSet: Int,_listener: ListSetDialogListener) : DialogFragment() {
    internal var listener: ListSetDialogListener = _listener

    interface ListSetDialogListener {
        fun onDialogListItem(dialog:DialogFragment, position: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = requireParentFragment().let{
            AlertDialog.Builder(it.context)
        }

        builder.setItems(itemSet,DialogInterface.OnClickListener { dialog, which ->
            listener.onDialogListItem(this, which)
            dialog.dismiss()
        })


        return builder.create()
    }


}