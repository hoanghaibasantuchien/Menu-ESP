package com.axion.menuoverlay

import android.graphics.Bitmap
import android.graphics.Rect

object EspEngine {

    init {
        // Bật lại thư viện C++
        System.loadLibrary("espcore")
    }

    // Vùng minimap mặc định (sẽ được ghi đè từ MainActivity)
    var minimapRegion: Rect = Rect(0, 0, 0, 0)
    
    var hMin = 340; var hMax = 20
    var sMin = 120
    var vMin = 90
    var minBlobPx = 6
    var maxBlobPx = 400

    data class Blob(val cx: Int, val cy: Int, val w: Int, val h: Int)

    fun detect(bmp: Bitmap): List<Blob> {
        val region = if (minimapRegion.width() > 0) minimapRegion
                     else Rect(0, 0, bmp.width, bmp.height)

        val x0 = region.left.coerceIn(0, bmp.width - 1)
        val y0 = region.top.coerceIn(0, bmp.height - 1)
        val x1 = region.right.coerceIn(x0 + 1, bmp.width)
        val y1 = region.bottom.coerceIn(y0 + 1, bmp.height)
        val w = x1 - x0
        val h = y1 - y0
        if (w <= 0 || h <= 0) return emptyList()

        val crop = Bitmap.createBitmap(bmp, x0, y0, w, h)
        val pixels = IntArray(w * h)
        crop.getPixels(pixels, 0, w, 0, 0, w, h)
        crop.recycle()

        val raw = nativeDetectRedBlobs(
            pixels, w, h,
            minBlobPx, maxBlobPx,
            hMin, hMax, sMin, vMin
        )

        val blobs = ArrayList<Blob>(raw.size / 4)
        var i = 0
        while (i + 3 < raw.size) {
            blobs.add(Blob(
                raw[i] + x0, raw[i + 1] + y0,
                raw[i + 2], raw[i + 3]
            ))
            i += 4
        }
        return blobs
    }

    private external fun nativeDetectRedBlobs(
        pixels: IntArray, width: Int, height: Int,
        minBlobPx: Int, maxBlobPx: Int,
        hMin: Int, hMax: Int, sMin: Int, vMin: Int
    ): IntArray
}
