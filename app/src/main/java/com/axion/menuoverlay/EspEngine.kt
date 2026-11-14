package com.axion.menuoverlay

import android.graphics.Bitmap
import android.graphics.Rect

object EspEngine {

    init {
        // System.loadLibrary("espcore") // TẠM TẮT ĐỂ TEST LỖI VĂNG
    }

    var minimapRegion: Rect = Rect(0, 0, 0, 0)
    var hMin = 340; var hMax = 20
    var sMin = 120
    var vMin = 90
    var minBlobPx = 6
    var maxBlobPx = 400

    data class Blob(val cx: Int, val cy: Int, val w: Int, val h: Int)

    fun detect(bmp: Bitmap): List<Blob> {
        // Trả về rỗng tạm thời vì chưa có C++
        return emptyList()
    }

    private external fun nativeDetectRedBlobs(
        pixels: IntArray, width: Int, height: Int,
        minBlobPx: Int, maxBlobPx: Int,
        hMin: Int, hMax: Int, sMin: Int, vMin: Int
    ): IntArray
}
