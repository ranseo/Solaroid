package com.ranseo.solaroid.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.ranseo.solaroid.firebase.FirebaseManager
import com.ranseo.solaroid.repositery.log.LogRepositery
import com.ranseo.solaroid.ui.login.activity.LoginActivity
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
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


    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun loadImage(imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {

            var bmp: Bitmap? = null
            val TAG = "loadImage"
            try {
                val url = URL(imageUrl)
                Log.i(TAG, "URL : ${url}")


                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 30000


                var inputStream = connection.inputStream
                val bufferedInputStream = BufferedInputStream(inputStream)
                bufferedInputStream.mark(100)

                val exif = ExifInterface(bufferedInputStream)



                bufferedInputStream.reset()
                val bitmap = BitmapFactory.decodeStream(bufferedInputStream)



                bmp = resizeBitmap(exif, bitmap)

                inputStream.close()
                bufferedInputStream.close()
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            bmp
        }
    }

    suspend fun loadImage2(imageUrl: String) {

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


    @RequiresApi(Build.VERSION_CODES.N)
    fun resizeBitmap(exif: ExifInterface, bitmap:Bitmap) : Bitmap? {
        val TAG  = "resizeBitmap"

        val orientation : Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED)
        val matrix = Matrix()
        when(orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                Log.i(TAG,"ORIENTATION_ROTATE_90")
                matrix.postRotate(90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                Log.i(TAG,"ORIENTATION_ROTATE_180")
                matrix.postRotate(180f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                Log.i(TAG,"ORIENTATION_ROTATE_270")
                matrix.postRotate(270f)
            }
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                Log.i(TAG,"ORIENTATION_FLIP_HORIZONTAL")
                matrix.setScale(-1f,1f)
            }
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                Log.i(TAG,"ORIENTATION_FLIP_VERTICAL")
                matrix.postRotate(180f)
                matrix.postScale(-1f,1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                Log.i(TAG,"ORIENTATION_TRANSPOSE")
                matrix.setRotate(90f)
                matrix.postScale(-1f,1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                Log.i(TAG,"ORIENTATION_TRANSVERSE ")
                matrix.postRotate(90f)
                matrix.postScale(-1f,1f)
            }
            ExifInterface.ORIENTATION_NORMAL -> {
                Log.i(TAG,"ORIENTATION_NORMAL")
                matrix.postRotate(90f)
            }
            ExifInterface.ORIENTATION_UNDEFINED -> {
                Log.i(TAG,"ORIENTATION_UNDEFINED")
                matrix.postRotate(0f)
            }
            else -> {
                Log.i(TAG,"ORIENTATION_ELSE")
                matrix.postRotate(90f)
            }
        }

        return Bitmap.createBitmap(bitmap, 0,0, bitmap.width, bitmap.height, matrix, true)

    }
}


