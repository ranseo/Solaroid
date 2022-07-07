package com.example.solaroid.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.BitmapCompat
import java.io.ByteArrayOutputStream

object BitmapUtils {

    @Synchronized
    suspend fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        return stream.toByteArray()
    }

    @Synchronized
    suspend fun convertUrlToByteArray(uri: Uri, context: Context) : ByteArray? = context.contentResolver.openInputStream(uri)?.buffered()?.use{it.readBytes()}



    fun convertByteArrayToBitmap(byteArray: ByteArray) : Bitmap {
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }

}