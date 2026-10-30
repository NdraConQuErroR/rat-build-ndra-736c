package com.app.cyber

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper

object FlashlightController {

    private var mgr: CameraManager? = null
    private var id: String? = null
    private var blinkHandler: Handler? = null

    private fun init(ctx: Context) {
        if (mgr == null) {
            mgr = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            id = mgr?.cameraIdList?.firstOrNull()
        }
    }

    fun set(ctx: Context, on: Boolean) {
        init(ctx)
        try {
            mgr?.setTorchMode(id ?: return, on)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun blink(ctx: Context, ms: Int) {
        init(ctx)
        blinkHandler = Handler(Looper.getMainLooper())
        var state = false
        
        val r = object : Runnable {
            override fun run() {
                state = !state
                set(ctx, state)
                blinkHandler?.postDelayed(this, ms.toLong())
            }
        }
        blinkHandler?.post(r)
    }
}
