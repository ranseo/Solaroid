package com.example.solaroid.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.solaroid.models.domain.RequestAlbum
import java.lang.IllegalStateException

class RequestAlbumAcceptDialogFragment(_listener: RequestAlbumAcceptDialogListener, val requestAlbum: RequestAlbum ,val titleMsg:String, val positiveMsg: String, val negativeMsg:String) : DialogFragment() {
    internal var listener: RequestAlbumAcceptDialogListener = _listener

    interface RequestAlbumAcceptDialogListener {
        fun onDialogPositiveClick(requestAlbum: RequestAlbum, dialog: DialogFragment)
        fun onDialogNegativeClick(requestAlbum: RequestAlbum, dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return requireParentFragment().let {
            val builder = AlertDialog.Builder(it.context)
            builder?.setMessage(titleMsg)
                .setPositiveButton(positiveMsg, DialogInterface.OnClickListener { dialogInterface, i ->
                    listener.onDialogPositiveClick(requestAlbum,this)
                })
                .setNegativeButton(negativeMsg, DialogInterface.OnClickListener { dialogInterface, i ->
                    listener.onDialogNegativeClick(requestAlbum, this)
                })
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}