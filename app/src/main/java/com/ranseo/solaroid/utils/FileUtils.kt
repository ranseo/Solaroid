package com.ranseo.solaroid.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.ranseo.solaroid.ui.home.fragment.frame.SolaroidFrameFragment
import java.io.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private const val TAG = "FileUtils"

//    fun makeCacheDir(inputUri:Uri, context: Context, exif:ExifInterface?): Uri {
//        val imagePath = File(context.cacheDir, "my_profile_images")
//        imagePath.mkdirs()
//        val fileName =
//            SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA).format(System.currentTimeMillis())
//        val newFile = File(imagePath, "${fileName}.jpeg")
//
//        val uri = FileProvider.getUriForFile(
//            context,
//            "com.ranseo.solaroid.fileprovider",
//            newFile
//        )
//
//        val packageName = context.packageName
//        Log.i(TAG, "packageName : ${packageName}")
//        context.grantUriPermission(
//            packageName,
//            uri,
//            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//        )
//
//        try {
//            Log.i(TAG, "URI : ${uri}")
//            context.contentResolver.openFileDescriptor(uri, "w", null).use {
//                FileOutputStream(it!!.fileDescriptor).use { output ->
//                    val input = BufferedInputStream(context.contentResolver.openInputStream(inputUri))
//                    var data = input.read()
//
//                    while(data!=-1) {
//                        output.write(data)
//                        data = input.read()
//                    }
//
//
//                    input.close()
//                    output.flush()
//                    output.close()
//
//                }
//            }
//
//
//            Log.i(TAG, "success")
//        } catch (error: Exception) {
//            Log.e(TAG, "makeCacheDir() error: ${error}")
//        } catch (error: IOException) {
//            error.printStackTrace()
//        } catch (error: FileNotFoundException) {
//            error.printStackTrace()
//        }
//
//
//        context.revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//        return uri
//    }


    fun getExifAttributeOrientation(uri:Uri, context:Context) : Int {
        val input = BufferedInputStream(context.contentResolver.openInputStream(uri))
        val exif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ExifInterface(input)
        } else {
            Log.e(TAG, "current api version not apply with this functions (getExifAttributeOrientation)")
            return ExifInterface.ORIENTATION_UNDEFINED
        }

        input.close()


        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_270)
        Log.i(TAG,"exif value : Orientation : ${orientation}")

        return orientation
    }
}