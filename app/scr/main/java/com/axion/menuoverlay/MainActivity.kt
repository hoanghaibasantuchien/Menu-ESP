package com.axion.menuoverlay

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private var projectionGranted = false
    private var projectionData: Intent? = null
    private var projectionResultCode: Int = Activity.RESULT_CANCELED

    private val projectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                projectionGranted = true
                projectionData = result.data
                projectionResultCode = result.resultCode
                Toast.makeText(this, "Đã cấp quyền chụp màn hình", Toast.LENGTH_SHORT).show()
                status.text = "Đã đủ quyền — bấm BẬT MENU"
                status.setTextColor(0xFF66FF66.toInt())
            } else {
                Toast.makeText(this, "Từ chối quyền chụp màn hình", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 120, 60, 60)
            setBackgroundColor(0xFF000000.toInt())
        }

        val titleView = TextView(this).apply {
            text = "MENU ESP BY HOANG HAI"
            textSize = 20f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 0, 0, 40)
        }

        status = TextView(this).apply {
            text = "Bước 1 → 2 → 3 → 4"
            textSize = 14f
            setTextColor(0xFFFFAA66.toInt())
            setPadding(0, 0, 0, 40)
        }

        val grantBtn = Button(this).apply {
            text = "1. Cấp quyền vẽ trên ứng dụng khác"
            setOnClickListener {
                startActivity(Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ))
            }
        }

        val batteryBtn = Button(this).apply {
            text = "2. Bỏ tối ưu pin"
            setOnClickListener {
                val pm = getSystemService(POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    startActivity(Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")
                    ))
                }
            }
        }

        val projectionBtn = Button(this).apply {
            text = "3. Cấp quyền chụp màn hình"
            setOnClickListener {
                val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                projectionLauncher.launch(mgr.createScreenCaptureIntent())
            }
        }

        val startBtn = Button(this).apply {
            text = "4. BẬT MENU + ESP"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    Toast.makeText(this@MainActivity, "Chưa có quyền overlay", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!projectionGranted || projectionData == null) {
                    Toast.makeText(this@MainActivity, "Chưa cấp quyền chụp màn hình", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                ContextCompat.startForegroundService(
                    this@MainActivity,
                    Intent(this@MainActivity, OverlayService::class.java)
                )
                ScreenCaptureService.start(
                    this@MainActivity,
                    projectionResultCode,
                    projectionData!!
                )
                Toast.makeText(this@MainActivity, "Menu + ESP đã bật", Toast.LENGTH_SHORT).show()
                moveTaskToBack(true)
            }
        }

        val stopBtn = Button(this).apply {
            text = "TẮT MENU"
            setOnClickListener {
                stopService(Intent(this@MainActivity, OverlayService::class.java))
                stopService(Intent(this@MainActivity, ScreenCaptureService::class.java))
                Toast.makeText(this@MainActivity, "Đã tắt", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(titleView)
        layout.addView(status)
        layout.addView(grantBtn)
        layout.addView(batteryBtn)
        layout.addView(projectionBtn)
        layout.addView(startBtn)
        layout.addView(stopBtn)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(this)) {
            status.text = "Đã có quyền overlay — sẵn sàng"
            status.setTextColor(0xFF66FF66.toInt())
        } else {
            status.text = "Chưa cấp quyền overlay"
            status.setTextColor(0xFFFF6666.toInt())
        }
    }
}
