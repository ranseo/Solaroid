package com.example.solaroid.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.solaroid.R
import com.example.solaroid.solaroidframe.SolaroidFrameViewModel

class ListSetDialogFragment(_listener: ListSetDialogListener, val viewModel: SolaroidFrameViewModel) : DialogFragment() {
    internal var listener: ListSetDialogListener = _listener

    interface ListSetDialogListener {
        fun onDialogListItem(dialog:DialogFragment, position: Int, viewModel: SolaroidFrameViewModel)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = requireParentFragment().let{
            AlertDialog.Builder(it.context)
        }
        builder?.setItems(R.array.list_set_dialog_items,DialogInterface.OnClickListener { dialog, which ->
            listener.onDialogListItem(this, which, viewModel)
            dialog.dismiss()
        })


        return builder.create()
    }


}