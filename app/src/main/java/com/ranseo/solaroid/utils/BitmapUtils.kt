package com.ranseo.solaroid.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
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
    suspend fun convertUrlToBitmap(url: String): Bitmap? =
        suspendCancellableCoroutine { continuation ->
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
    fun convertUriToBitmap(uri: Uri, context: Context): Bitmap {
        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
    }


    fun convertByteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


    suspend fun loadImage(imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {

            var bmp: Bitmap? = null
            val TAG = "loadImage"
            try {
                val url = URL(imageUrl)
                Log.i(TAG, "URL : ${url}")
                val stream = url.openStream()
                Log.i(TAG, "stream : ${stream}")
                bmp = BitmapFactory.decodeStream(stream)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            bmp
        }
    }

    // Bitmap -> String
    fun bitmapToString(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        val bytes = stream.toByteArray()


        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    // String -> Bitmap
    fun stringToBitmap(encodedString: String): Bitmap {
        val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    }
}


