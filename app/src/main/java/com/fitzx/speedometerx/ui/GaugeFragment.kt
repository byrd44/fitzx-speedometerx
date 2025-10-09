package com.fitzx.speedometerx.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fitzx.speedometerx.R
import kotlin.math.roundToInt

class GaugeFragment : Fragment() {

    private var mphView: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_gauge, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mphView = view.findViewById(R.id.gaugeMph)
    }

    fun updateSpeed(mps: Float) {
        val mph = (mps * 2.2369363f).roundToInt()
        mphView?.text = mph.toString()
    }
}
