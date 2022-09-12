package com.ranseo.solaroid.custom.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ranseo.solaroid.parseProfileImgStringToList
import com.ranseo.solaroid.utils.BitmapUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

//Coordinator Compute
typealias CC = Pair<Float, Float>

@RequiresApi(Build.VERSION_CODES.P)
class AlbumThumbnailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val TAG = "AlbumThumbnailView"

    private var radius = 0.0f
    private var length = 0.0f

    private val computationList = listOf(
        listOf(CC(0.5F, 0.5F)),
        listOf(CC(0.35F, 0.35F), CC(0.65F, 0.65F)),
        listOf(CC(0.5F, 0.28F), CC(0.27F, 0.70F), CC(0.73F, 0.70F)),
        listOf(CC(0.25F, 0.25F), CC(0.75F, 0.25F), CC(0.25F, 0.75F), CC(0.75F, 0.75F))
    )

    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    var participants = 0

    var thumbnailString: String = ""
    private var thumbnailList: MutableList<Bitmap> = mutableListOf()
    private var thumbnailUri: Uri? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }


    init {
//        context.withStyledAttributes(attrs, R.styleable.AlbumThumbnailView) {
////            participants = (getString(R.styleable.AlbumThumbnailView_participants)?.toInt() ?: 1) - 1
////            thumbnailString = getString(R.styleable.AlbumThumbnailView_thumbnail).toString()
//            for(str in parseProfileImgStringToList(thumbnailString)) {
//                thumbnailList.add(BitmapUtils.convertUriToBitmap(str.toUri(), context))
//            }
//        }


    }


    private fun PointF.computeXYForThumbnail(cc: CC) {
        x = width * cc.first
        y = width * cc.second
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        length = (w / 2).toFloat()
        radius = (StrictMath.min(w, h) / 4).toFloat() * when (participants) {
            0 -> 1.5f
            1 -> 1.3f
            2 -> 1.1f
            else -> 1f
        }


        if (!thumbnailString.isNullOrEmpty()) {
            coroutineScope.launch {
                Log.i(TAG, "thumbnnailString :${thumbnailString}")

                for (str in parseProfileImgStringToList(thumbnailString)) {
                    launch(Dispatchers.IO) {
                        thumbnailList.add(BitmapUtils.loadImage(str)!!)

                    }.join()
                }

                Log.i(TAG, "thumbnailList :${thumbnailList}")

                for (i in 0..participants) {
                    launch {
                        thumbnailList[i] = Bitmap.createScaledBitmap(thumbnailList[i], w, h, true)
                    }.join()

                }
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        try {

            paint.color = Color.BLACK

            for (i in 0..participants) {
                pointPosition.computeXYForThumbnail(computationList[participants][i])
                Log.i(TAG, "onDraw() : thumbnailList : ${thumbnailList[i]}")
                val rect = Rect(
                    (pointPosition.x - length / 2).toInt(),
                    (pointPosition.y - length / 2).toInt(),
                    (pointPosition.x + length / 2).toInt(),
                    (pointPosition.y + length / 2).toInt()
                )

                //canvas!!.drawRect(rect,paint)

                //canvas!!.drawBitmap(bitmap!!, null, rect, paint)
                Log.i(TAG, "width: ${width}, height : ${height}")
                canvas!!.drawBitmap(thumbnailList[i].getCircledBitmap(), null, rect, paint)
            }


        } catch (error: Exception) {
            Log.i(TAG, "error : ${error.message}")
        }
    }

    fun Bitmap.getCircledBitmap(): Bitmap {
        Log.i(TAG, "Bitmap.getCircledBitmap() = width: ${this.width}, height : ${this.height}")
        val output = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val rect = Rect(0, 0, this.width, this.height)
        //canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF)
        //canvas.drawARGB(255, 139, 197, 186)

        paint.color = Color.WHITE

        canvas.drawCircle(
            (this.width / 2).toFloat(),
            (this.height / 2).toFloat(),
            (this.width / 2).toFloat(),
            paint
        )
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        paint.color = Color.BLUE
        canvas.drawBitmap(this, rect, rect, paint)
        paint.xfermode = null
        return output
    }

}

fun AlbumThumbnailView.getBitmapFromView(): Bitmap {
    val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.draw(canvas)
    return bitmap
}