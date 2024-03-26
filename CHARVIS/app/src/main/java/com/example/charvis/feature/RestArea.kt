package com.example.charvis.feature

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.example.charvis.model.Point
import kotlin.math.pow
import kotlin.math.sqrt

fun distance(p1: Point, p2: Point): Double {
    return sqrt((p1.latitude - p2.latitude).pow(2) + (p1.longtitude - p2.longtitude).pow(2))
}

fun findNearestNeighbor(target: Point, points: List<Point>): Point? {
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

fun searchLoadToTMap(context: Context, startLocation: Point, endLocation: Point?) {
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

