package org.datumpoint.app.core.gnss

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.GnssMeasurementsEvent
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.datumpoint.app.core.model.LocationSample

data class GnssDiagnostics(
    val satellitesVisible: Int? = null,
    val satellitesUsed: Int? = null,
    val meanCn0: Double? = null,
    val rawMeasurementsSupported: Boolean = false,
    val constellations: Set<Int> = emptySet(),
)

class GnssLocationSource(private val context: Context) {
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun hasFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun isGpsEnabled(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    fun rawMeasurementsAvailable(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        } else {
            false
        }

    @SuppressLint("MissingPermission")
    fun observeSamples(): Flow<LocationSample> = callbackFlow {
        require(hasFineLocationPermission()) { "ACCESS_FINE_LOCATION is required." }
        var diagnostics = GnssDiagnostics(rawMeasurementsSupported = rawMeasurementsAvailable())

        val statusCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val cn0Values = mutableListOf<Double>()
                var used = 0
                val constellations = mutableSetOf<Int>()
                for (index in 0 until status.satelliteCount) {
                    if (status.usedInFix(index)) used++
                    cn0Values += status.getCn0DbHz(index).toDouble()
                    constellations += status.getConstellationType(index)
                }
                diagnostics = diagnostics.copy(
                    satellitesVisible = status.satelliteCount,
                    satellitesUsed = used,
                    meanCn0 = cn0Values.takeIf { it.isNotEmpty() }?.average(),
                    constellations = constellations,
                )
            }
        }
        val measurementCallback = object : GnssMeasurementsEvent.Callback() {}
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (location.provider != LocationManager.GPS_PROVIDER || !location.hasAccuracy()) return
                trySend(
                    LocationSample(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitude = if (location.hasAltitude()) location.altitude else null,
                        accuracyMeters = location.accuracy.toDouble(),
                        timestampMillis = location.time,
                        provider = location.provider ?: "gps",
                        satellitesVisible = diagnostics.satellitesVisible,
                        satellitesUsed = diagnostics.satellitesUsed,
                        meanCn0 = diagnostics.meanCn0,
                    ),
                )
            }

            @Deprecated("Deprecated by Android framework")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }

        locationManager.registerGnssStatusCallback(statusCallback)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationManager.registerGnssMeasurementsCallback(measurementCallback)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, listener)
        awaitClose {
            locationManager.removeUpdates(listener)
            locationManager.unregisterGnssStatusCallback(statusCallback)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locationManager.unregisterGnssMeasurementsCallback(measurementCallback)
            }
        }
    }
}
