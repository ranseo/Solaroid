package com.example.solaroid.utils

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.BitmapCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

object BitmapUtils {

    @Synchronized
    suspend fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        return stream.toByteArray()
    }

    @Synchronized
    suspend fun convertUriToByteArray(uri: Uri, context: Context): ByteArray? =
        context.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() }


    @Synchronized
    suspend fun convertUrlToBitmap(url: String): Bitmap? = suspendCancellableCoroutine { continuation ->
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        GlobalScope.launch(Dispatchers.IO) {
            try {
                if (urlConnection.responseCode == 200) {
                    val stream = BufferedInputStream(urlConnection.inputStream)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    continuation.resume(bitmap) {}
                } else {

                }
            } catch (e: Exception) {

            } finally {
                urlConnection.disconnect()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun convertUriToBitmap(uri:Uri, context: Context) : Bitmap {
        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
    }


    fun convertByteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

}