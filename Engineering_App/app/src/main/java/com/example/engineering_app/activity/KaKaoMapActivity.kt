package com.example.engineering_app.activity

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.engineering_app.databinding.ActivityKakaoMapBinding
import net.daum.mf.map.api.MapView

class KaKaoMapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKakaoMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKakaoMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val mapView = MapView(this)
        val mapViewContainer = binding.mapView as ViewGroup
        mapViewContainer.addView(mapView)

    }

}