package com.example.solaroid.custom.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.example.solaroid.R

class AlbumThumbnailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?=null,
    defStyleAttr: Int =0
): View(context, attrs, defStyleAttr) {
    private val TAG = "AlbumThumbnailView"

    private var radius = 0.0f
    private var length = 0.0f

    private val pointPosition: PointF = PointF(0.0f,0.0f)

    private var participants = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }



    init {
        context.withStyledAttributes(attrs, R.styleable.AlbumThumbnailView) {
            participants = getInteger(R.styleable.AlbumThumbnailView_participants,1)
        }

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        length = (w/2).toFloat()
        radius = (w/4).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = Color.RED

        try{
            canvas!!.drawRect(length,length,length,length,paint)

        } catch (error:Exception) {

        }
    }




}