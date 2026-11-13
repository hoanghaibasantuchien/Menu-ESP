package com.axion.menuoverlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

@SuppressLint("ClickableViewAccessibility")
class FloatingMenu(
    private val ctx: Context,
    private val wm: WindowManager
) {

    private var root: LinearLayout? = null
    private var body: ScrollView? = null
    private var collapsed = false

    private var params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        overlayType(),
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 80
        y = 200
    }

    fun show() {
        if (root != null) return
        root = buildMenu().also { wm.addView(it, params) }
    }

    fun hide() {
        root?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
        }
        root = null
    }

    private fun buildMenu(): LinearLayout {
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E6000000"))
                setStroke(dp(1), Color.WHITE)
                cornerRadius = dp(10).toFloat()
            }
            setPadding(dp(12), dp(8), dp(12), dp(10))
        }

        val headerRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(2), 0, dp(6))
        }

        val title = TextView(ctx).apply {
            text = "MENU ESP BY HOANG HAI"
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setPadding(0, dp(2), dp(10), dp(2))
        }

        val collapseBtn = TextView(ctx).apply {
            text = "[–]"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(6), dp(0), dp(6), dp(0))
            setOnClickListener { toggleCollapse() }
        }

        headerRow.addView(title, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        headerRow.addView(collapseBtn)

        val divider = View(ctx).apply {
            setBackgroundColor(Color.parseColor("#66FFFFFF"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            )
        }

        val bodyLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(4))
        }

        addSwitch(bodyLayout, "ESP", MenuConfig.espEnabled) { MenuConfig.espEnabled = it }
        addSwitch(bodyLayout, "Box", MenuConfig.espBox) { MenuConfig.espBox = it }
        addSwitch(bodyLayout, "Distance", MenuConfig.espDistance) { MenuConfig.espDistance = it }

        body = ScrollView(ctx).apply {
            addView(bodyLayout)
            layoutParams = LinearLayout.LayoutParams(dp(240), dp(220))
        }

        val footer = TextView(ctx).apply {
            text = "by HOANG HAI"
            setTextColor(Color.parseColor("#99FFFFFF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(2))
        }

        container.addView(headerRow)
        container.addView(divider)
        container.addView(body)
        container.addView(footer)

        attachDrag(headerRow)
        return container
    }

    private fun addSwitch(parent: LinearLayout, label: String, initial: Boolean, onChange: (Boolean) -> Unit) {
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(3), 0, dp(3))
        }

        val tv = TextView(ctx).apply {
            text = label
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }

        val sw = Switch(ctx).apply {
            isChecked = initial
            setOnCheckedChangeListener { _, v -> onChange(v) }
        }

        row.addView(tv, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(sw)
        parent.addView(row)
    }

    private fun toggleCollapse() {
        collapsed = !collapsed
        body?.visibility = if (collapsed) View.GONE else View.VISIBLE
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachDrag(view: View) {
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f

        view.setOnTouchListener { _, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    touchX = ev.rawX
                    touchY = ev.rawY
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (ev.rawX - touchX).toInt()
                    params.y = startY + (ev.rawY - touchY).toInt()
                    root?.let { wm.updateViewLayout(it, params) }
                    true
                }
                else -> false
            }
        }
    }

    private fun overlayType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

    private fun dp(v: Int): Int =
        (v * ctx.resources.displayMetrics.density).toInt()
}
