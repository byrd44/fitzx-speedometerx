package com.fitzx.speedometerx.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fitzx.speedometerx.R
import kotlin.math.roundToInt

class DigitalFragment : Fragment() {

    private var mphView: TextView? = null
    private var kmhView: TextView? = null
    private var latView: TextView? = null
    private var lonView: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_digital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mphView = view.findViewById(R.id.d_mph)
        kmhView = view.findViewById(R.id.d_kmh)
        latView = view.findViewById(R.id.d_lat)
        lonView = view.findViewById(R.id.d_lon)
    }

    fun updateLocation(lat: Double, lon: Double) {
        latView?.text = "lat: ${"%.5f".format(lat)}"
        lonView?.text = "lon: ${"%.5f".format(lon)}"
    }

    fun updateSpeed(mps: Float) {
        val mph = (mps * 2.2369363f).roundToInt()
        val kmh = (mps * 3.6f).roundToInt()
        mphView?.text = "$mph mph"
        kmhView?.text = "$kmh km/h"
    }
}
