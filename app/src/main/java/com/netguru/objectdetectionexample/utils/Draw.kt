package com.netguru.objectdetectionexample.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

@SuppressLint("ViewConstructor")
class Draw(context: Context?, var rect: Rect, var name: String, var confidence: Float?) : View(context) {

    private lateinit var boundaryPaint: Paint
    private lateinit var textPaint: Paint

    private val textPadding = 16

    init {
        paintInit()
    }

    private fun paintInit() {
        boundaryPaint = Paint()
        boundaryPaint.color = Color.GREEN
        boundaryPaint.strokeWidth = 10f
        boundaryPaint.style = Paint.Style.STROKE

        textPaint = Paint()
        textPaint.color = Color.GREEN
        textPaint.textSize = 50f
        textPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawText(getTextInformation(), rect.left.toFloat(), rect.top.toFloat() - textPadding, textPaint)
        canvas?.drawRect(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat(), boundaryPaint)
    }

    private fun getTextInformation() = if (confidence != null) "$name ${String.format("%.02f", confidence!! * 100f)}%" else name


}
