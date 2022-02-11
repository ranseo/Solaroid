package com.example.solaroid.solaroidcreate

import android.net.Uri
import android.widget.EditText
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.solaroid.R


//@BindingAdapter("onTextChagned")
//fun onEditTextChanged(editText: EditText, viewModel: SolaroidPhotoCreateViewModel) {
//    val text = editText.text.toString()
//    when(editText.id) {
//        R.id.front_text -> {
//            viewModel.setFrontText(text)
//        }
//        R.id.back_text -> {
//            viewModel.setBackText(text)
//        }
//
//    }
//}
//


@BindingAdapter("setImage")
fun bindImage (imageView: ImageView, imgUri: Uri?) {
    imgUri?.let {

        Glide.with(imageView.context)
            .load(imgUri)
            .into(imageView)
    }
}
