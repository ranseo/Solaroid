package com.example.solaroid.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.solaroid.R
import com.example.solaroid.databinding.FragmentAlbumBinding
import com.example.solaroid.databinding.FragmentAlbumCreateBinding
import java.lang.IllegalStateException

class AlbumCreateDialog(_listener:AlbumCreateDialogListener) : DialogFragment() {
    internal var listener: AlbumCreateDialogListener = _listener

    private lateinit var binding : FragmentAlbumCreateBinding

    interface AlbumCreateDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_album_create,container,false)
        



        return binding.root
    }
}