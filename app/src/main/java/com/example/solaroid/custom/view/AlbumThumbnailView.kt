package com.example.solaroid.custom.view

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.withStyledAttributes
import androidx.core.net.toUri
import com.example.solaroid.R
import com.example.solaroid.parseProfileImgStringToList
import com.example.solaroid.utils.BitmapUtils

//Coordinator Compute
typealias CC = Pair<Float, Float>

@RequiresApi(Build.VERSION_CODES.P)
class AlbumThumbnailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
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

    private var participants = 0

    private var thumbnailString : String = ""
    private var thumbnailList : MutableList<Bitmap> = mutableListOf()
    private var thumbnailUri : Uri? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }


    init {
        context.withStyledAttributes(attrs, R.styleable.AlbumThumbnailView) {
            participants = getInteger(R.styleable.AlbumThumbnailView_participants, 1) - 1
            thumbnailString = getString(R.styleable.AlbumThumbnailView_thumbnail).toString()
            for(str in parseProfileImgStringToList(thumbnailString)) {
                thumbnailList.add(BitmapUtils.convertUriToBitmap(str.toUri(), context))
            }
        }


    }

    private fun PointF.computeXYForThumbnail(cc:CC) {
        x = width * cc.first
        y = width * cc.second
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        length = (w / 2).toFloat()
        radius = (StrictMath.min(w, h) / 4).toFloat() * when(participants) {
            0 -> 1.5f
            1 -> 1.3f
            2 -> 1.1f
            else -> 1f
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        try {

            paint.color = Color.BLACK

            for(i in 0..participants) {
                pointPosition.computeXYForThumbnail(computationList[participants][i])
                thumbnailList[i].getCircledBitmap()
                canvas!!.drawCircle(pointPosition.x,pointPosition.y,radius,paint)
            }


        } catch (error: Exception) {
            Log.i(TAG, "error : ${error.message}")
        }
    }

    fun Bitmap.getCircledBitmap(x:Float, y:Float, radius:Float): Bitmap {
        val output = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, this.width, this.height)
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(x, y, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(this, rect, rect, paint)
        return output
    }


}