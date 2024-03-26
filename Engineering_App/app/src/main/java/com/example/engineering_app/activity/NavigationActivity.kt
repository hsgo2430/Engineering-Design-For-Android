package com.example.engineering_app.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.example.engineering_app.R
import com.example.engineering_app.databinding.ActivityNavigationBinding
import com.example.engineering_app.model.Point
import kotlin.math.pow
import kotlin.math.sqrt

class NavigationActivity : AppCompatActivity(){

    private lateinit var binding: ActivityNavigationBinding

    private lateinit var current_position: Point;

    private lateinit var sleepyRestArea: ArrayList<Point>

    private lateinit var locationManager: LocationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sleepyRestArea = getXml()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager



        initView()
    }

    private fun initView(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }

        binding.customPosition.setOnClickListener {
            binding.latitudeTextview.text = binding.latitudeEditview.text.toString()
            binding.lontitudeTextview.text = binding.lontitudeEditview.text.toString()

            current_position = Point("현재위치",binding.latitudeEditview.text.toString().toDouble(), binding.lontitudeEditview.text.toString().toDouble())
        }

        binding.currentPosition.setOnClickListener {
            binding.latitudeTextview.text = "검색중..."
            binding.lontitudeTextview.text = "검색중..."
            getCurrentPosition()
        }

        binding.goToNavigation.setOnClickListener {

            val nearestNeighbor = findNearestNeighbor(current_position, sleepyRestArea)
            binding.closestPosition.text = "$nearestNeighbor"
            //Log.d("로그", "Nearest neighbor to $current_position is $nearestNeighbor")

            //RetrofitManager.instance.getSleepyRestArea()
            searchLoadToTMap(this, current_position, nearestNeighbor)
            getXml()

        }
    }

    private fun distance(p1: Point, p2: Point): Double {
        return sqrt((p1.latitude - p2.latitude).pow(2) + (p1.longtitude - p2.longtitude).pow(2))
    }

    private fun findNearestNeighbor(target: Point, points: List<Point>): Point? {
        if (points.isEmpty()) return null

        var nearest: Point? = null
        var minDistance = Double.MAX_VALUE

        for (point in points) {
            val dist = distance(target, point)
            if (dist < minDistance) {
                minDistance = dist
                nearest = point
            }
        }

        return nearest
    }

    private fun getXml():ArrayList<Point>{
        val parser: XmlResourceParser = resources.getXml(R.xml.sleepy_rest_area)
        val sleepyRestArea: ArrayList<Point> = ArrayList<Point>()
        val oneSleepyRestArea : ArrayList<String> = ArrayList<String>()
        var count = 0

        while (parser.eventType != XmlResourceParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlResourceParser.START_TAG -> {
                    val tagName = parser.name
                }
                XmlResourceParser.TEXT -> {
                    val text = parser.text
                    oneSleepyRestArea.add(text)
                    count++
                }
                XmlResourceParser.END_TAG -> {
                    val tagName = parser.name
                }
            }

            if(count == 3){
                count = 0
                sleepyRestArea.add(Point(oneSleepyRestArea[0], oneSleepyRestArea[1].toDouble(), oneSleepyRestArea[2].toDouble()))
                oneSleepyRestArea.clear()
            }
            parser.next()
        }
        return sleepyRestArea
    }

    private fun getCurrentPosition(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Request location updates
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, object :
                LocationListener {
                override fun onLocationChanged(location: Location) {
                    current_position = Point(
                        "현재위치",
                        location.latitude,
                        location.longitude
                    )
                    // Update UI with current location
                    binding.latitudeTextview.text = location.latitude.toString()
                    binding.lontitudeTextview.text = location.longitude.toString()
                }

                override fun onStatusChanged(
                    provider: String?,
                    status: Int,
                    extras: Bundle?
                ) {
                }

                override fun onProviderEnabled(provider: String) {
                }

                override fun onProviderDisabled(provider: String) {
                }
            }, null)
        }
    }

    private fun searchLoadToTMap(context: Context, startLocation: Point, endLocation: Point?) {
        val url = "tmap://route?startx=${startLocation.longtitude}&starty=${startLocation.latitude}&goalx=${endLocation?.longtitude}&goaly=${endLocation?.latitude}&reqCoordType=WGS84&resCoordType=WGS84"

        val intent =  Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)

        val installCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            context.packageManager.queryIntentActivities(
                Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER),
                PackageManager.GET_META_DATA
            )
        }

        // 티맵이 설치되어 있다면 앱으로 연결, 설치되어 있지 않다면 스토어로 이동
        if (installCheck.isEmpty()) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.skt.tmap.ku")))
        } else {
            context.startActivity(intent)
        }
    }
}