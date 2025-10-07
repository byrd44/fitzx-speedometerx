package com.fitzx.speedometerx

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private lateinit var mphView: TextView
    private lateinit var kmhView: TextView
    private lateinit var latView: TextView
    private lateinit var lonView: TextView

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            startLocationUpdatesIfPermitted()
        }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { onLocation(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mphView = findViewById(R.id.mph)
        kmhView = findViewById(R.id.kmh)
        latView = findViewById(R.id.lat)
        lonView = findViewById(R.id.lon)

        startLocationUpdatesIfPermitted()
    }

    private fun startLocationUpdatesIfPermitted() {
        val fineOk = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseOk = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineOk && !coarseOk) {
            requestPermission.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            return
        }

        val req = LocationRequest.create().apply {
            interval = 1000L
            fastestInterval = 500L
            smallestDisplacement = 0.5f
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        try { fused.requestLocationUpdates(req, locationCallback, mainLooper) } catch (_: SecurityException) {}
    }

    private fun onLocation(loc: Location) {
        val mps = loc.speed.coerceAtLeast(0f)
        val mph = (mps * 2.2369363f).roundToInt()
        val kmh = (mps * 3.6f).roundToInt()

        mphView.text = "$mph mph"
        kmhView.text = "$kmh km/h"
        latView.text = "lat: ${"%.5f".format(loc.latitude)}"
        lonView.text = "lon: ${"%.5f".format(loc.longitude)}"
    }

    override fun onDestroy() {
        super.onDestroy()
        fused.removeLocationUpdates(locationCallback)
    }
}
