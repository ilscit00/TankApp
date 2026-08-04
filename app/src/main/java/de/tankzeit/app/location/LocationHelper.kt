package de.tankzeit.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class GeoPoint(val lat: Double, val lng: Double)

class LocationHelper(private val context: Context) {

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Fragt die aktuelle GPS-Position ab. Wirft eine Exception, wenn keine
     * Berechtigung erteilt wurde - das UI sollte das vorher prüfen.
     */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): GeoPoint = suspendCancellableCoroutine { cont ->
        if (!hasLocationPermission()) {
            cont.resumeWithException(SecurityException("Standortberechtigung fehlt"))
            return@suspendCancellableCoroutine
        }
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(GeoPoint(location.latitude, location.longitude))
                } else {
                    cont.resumeWithException(IllegalStateException("Kein Standort verfügbar"))
                }
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    /**
     * Wandelt eine deutsche Postleitzahl per Geocoder in Koordinaten um.
     */
    @Suppress("DEPRECATION")
    suspend fun geocodePostalCode(postalCode: String): GeoPoint {
        val geocoder = Geocoder(context, Locale.GERMANY)
        val results = geocoder.getFromLocationName("$postalCode, Deutschland", 1)
        val first = results?.firstOrNull()
            ?: throw IllegalArgumentException("PLZ konnte nicht gefunden werden")
        return GeoPoint(first.latitude, first.longitude)
    }
}
