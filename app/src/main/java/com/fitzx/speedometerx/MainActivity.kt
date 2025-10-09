package com.fitzx.speedometerx

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fitzx.speedometerx.ui.DigitalFragment
import com.fitzx.speedometerx.ui.GaugeFragment
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private lateinit var gauge: GaugeFragment
    private lateinit var digital: DigitalFragment

    private var lastLoc: Location? = null
    private var lastTime: Long = 0L

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

        digital = DigitalFragment()
        gauge = GaugeFragment()

        val pager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.pager)
        val tabs = findViewById<TabLayout>(R.id.tabs)

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int) = when (position) {
                0 -> gauge
                else -> digital
            }
        }
        pager.adapter = adapter

        tabs.removeAllTabs()
        tabs.addTab(tabs.newTab().setText("Gauge"))
        tabs.addTab(tabs.newTab().setText("Digital"))

        tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { pager.currentItem = tab.position }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        pager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) { tabs.selectTab(tabs.getTabAt(position)) }
        })

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

        try {
            fused.requestLocationUpdates(req, locationCallback, mainLooper)
            fused.lastLocation.addOnSuccessListener { it?.let(::onLocation) }
        } catch (_: SecurityException) {}
    }

    private fun onLocation(loc: Location) {
        val now = System.currentTimeMillis()
        val reported = loc.speed

        val fallback = lastLoc?.let { prev ->
            val dt = (now - lastTime).coerceAtLeast(1L) / 1000.0
            val meters = haversineMeters(prev.latitude, prev.longitude, loc.latitude, loc.longitude)
            (meters / dt).toFloat()
        } ?: 0f

        val mps = if (reported > 0.3f) reported else fallback.coerceAtLeast(0f)

        digital.updateLocation(loc.latitude, loc.longitude)
        digital.updateSpeed(mps)
        gauge.updateSpeed(mps)

        lastLoc = loc
        lastTime = now
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat/2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon/2).pow(2.0)
        return 2 * R * asin(sqrt(a))
    }

    override fun onDestroy() {
        super.onDestroy()
        fused.removeLocationUpdates(locationCallback)
    }
}

