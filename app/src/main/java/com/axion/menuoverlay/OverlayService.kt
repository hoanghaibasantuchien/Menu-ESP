package com.axion.menuoverlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private var menu: FloatingMenu? = null
    private var espView: EspOverlayView? = null
    private var espParams: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        try {
            startForeground(NOTIF_ID, buildNotification())
        } catch (e: Exception) {
            Log.e("OverlayService", "Lỗi startForeground: ${e.message}")
        }

        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        try {
            espView = EspOverlayView(this)
            espParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP or Gravity.START }
            wm.addView(espView, espParams)

            menu = FloatingMenu(this, wm).also { it.show() }
        } catch (e: Exception) {
            Log.e("OverlayService", "Lỗi addView: ${e.message}")
        }
    }

    override fun onDestroy() {
        try {
            menu?.hide(); menu = null
            espView?.let { wm.removeView(it) }
            espView = null
        } catch (e: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "MENU ESP",
                NotificationManager.IMPORTANCE_LOW
            )
            val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            mgr.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MENU ESP BY HOANG HAI")
            .setContentText("Đang chạy")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIF_ID = 1001
        const val CHANNEL_ID = "menu_overlay_channel"
    }
}
