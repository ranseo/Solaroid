package com.ranseo.solaroid.custom.snackbar

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.ranseo.solaroid.R
import com.ranseo.solaroid.databinding.SnackbarNoneAlbumBinding


class CustomSnackBar(view: View, private val message: String, private val listener: CustomSnackBarInteface) {
    interface CustomSnackBarInteface {
        fun setOnClickListener() {}
    }

    companion object {
        fun make(view: View, message: String,listener:CustomSnackBarInteface) = CustomSnackBar(view, message, listener)
    }


    private val context = view.context
    private val snackBar = Snackbar.make(view, "", 5000)
    private val snackBarLayout = snackBar.view as Snackbar.SnackbarLayout

    private val inflater = LayoutInflater.from(context)
    private val snackBarBinding = DataBindingUtil.inflate<SnackbarNoneAlbumBinding>(
        inflater,
        R.layout.snackbar_none_album,
        null,
        false
    )

    init {
        initView()
        initData()
    }

    private fun initView() {
        with(snackBarLayout) {
            removeAllViews()
            setPadding(0,0,0,0)
            setBackgroundColor(ContextCompat.getColor(context,android.R.color.transparent))
            addView(snackBarBinding.root, 0)
        }
    }

    private fun initData() {
        snackBarBinding.tvText.text = message
        snackBarBinding.btnCreateAlbum.setOnClickListener {
            listener.setOnClickListener()
        }
    }

    fun show() {
        snackBar.show()
    }

}

