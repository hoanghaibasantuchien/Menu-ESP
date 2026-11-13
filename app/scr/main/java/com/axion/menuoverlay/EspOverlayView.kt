package com.axion.menuoverlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.view.View

class EspOverlayView(ctx: Context) : View(ctx) {

    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.RED
        isAntiAlias = true
    }
    private val labelPaint = Paint().apply {
        color = Color.WHITE
        textSize = 26f
        isAntiAlias = true
        setShadowLayer(4f, 0f, 0f, Color.BLACK)
    }

    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            invalidate()
            handler.postDelayed(this, 33)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(ticker)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!MenuConfig.espEnabled) return

        val blobs = EspBridge.blobs
        if (blobs.isEmpty()) return

        for (b in blobs) {
            if (MenuConfig.espBox) {
                canvas.drawRect(
                    (b.cx - b.w / 2f - 4),
                    (b.cy - b.h / 2f - 4),
                    (b.cx + b.w / 2f + 4),
                    (b.cy + b.h / 2f + 4),
                    boxPaint
                )
            }
            if (MenuConfig.espDistance) {
                canvas.drawText("●", (b.cx + b.w / 2f + 6), (b.cy.toFloat()), labelPaint)
            }
        }
    }
}
