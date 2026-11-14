package com.axion.menuoverlay

import android.graphics.Bitmap
import android.graphics.Rect

object EspEngine {

    // Đã tạm tắt C++ để test bằng Kotlin
    // init { System.loadLibrary("espcore") }

    var minimapRegion: Rect = Rect(0, 0, 0, 0)

    data class Blob(val cx: Int, val cy: Int, val w: Int, val h: Int)

    fun detect(bmp: Bitmap): List<Blob> {
        // Nếu chưa set vùng minimap thì lấy toàn màn hình
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

        val blobs = ArrayList<Blob>()

        // Thuật toán quét màu đỏ bằng Kotlin
        for (y in 0 until h step 4) {
            for (x in 0 until w step 4) {
                val p = pixels[y * w + x]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF

                // Nếu là màu đỏ (R cao, G và B thấp)
                if (r > 180 && g < 90 && b < 90) {
                    blobs.add(Blob(x + x0, y + y0, 12, 12)) // Vẽ box 12x12
                }
            }
        }
        return blobs
    }
}
