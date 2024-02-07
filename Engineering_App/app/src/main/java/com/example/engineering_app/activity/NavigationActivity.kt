package com.example.engineering_app.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.engineering_app.R
import com.example.engineering_app.databinding.ActivityMainBinding
import com.example.engineering_app.databinding.ActivityNavigationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNavigationBinding
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initView()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val koreatech = LatLng(36.763950, 127.281884)
        mMap.addMarker(
            MarkerOptions()
            .position(koreatech)
            .title("Marker in Koreatech")
        )
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10.0f))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(koreatech))


    }

    private fun initView(){
        binding.zoomIn.setOnClickListener {

        }

        binding.zoomOut.setOnClickListener {
            CameraUpdateFactory.zoomOut()
        }
    }
}