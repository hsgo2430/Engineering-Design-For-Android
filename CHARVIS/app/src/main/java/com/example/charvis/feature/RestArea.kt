package com.example.charvis.feature

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.charvis.model.Point
import kotlin.math.*

fun distance(p1: Point, p2: Point): Double {
    return sqrt((p1.latitude - p2.latitude).pow(2) + (p1.longitude - p2.longitude).pow(2))
}

fun angleBetweenVectors(v1: Point, v2: Point): Double {
    val dotProduct = v1.latitude * v2.latitude + v1.longitude * v2.longitude
    val magnitudeV1 = sqrt(v1.latitude.pow(2) + v1.longitude.pow(2))
    val magnitudeV2 = sqrt(v2.latitude.pow(2) + v2.longitude.pow(2))
    val cosTheta = dotProduct / (magnitudeV1 * magnitudeV2)
    return acos(cosTheta) * (180 / PI)  // 결과는 도(degree) 단위
}

fun findNearestNeighbor(target: Point, standardPoint: Point, points: List<Point>): Point? {

    if (points.isEmpty()) return null

    val targetVector = Point("방향벡터 1", target.latitude - standardPoint.latitude, target.longitude - standardPoint.longitude)
    var nearest: Point? = null
    var minDistance = Double.MAX_VALUE

    for (point in points) {
        val pointVector = Point("방향벡터 2", point.latitude - standardPoint.latitude, point.longitude - standardPoint.longitude)
        val angle = angleBetweenVectors(targetVector, pointVector)
        if (angle <= 45.0) {  // 각도가 45도 이하인 경우에만 계산
            Log.d("로그 벡터", point.toString() + angle.toString())
            val dist = distance(target, point)
            if (dist < minDistance) {
                minDistance = dist
                nearest = point
            }
        }
    }
    return nearest
}

fun searchLoadToTMap(context: Context, startLocation: Point, endLocation: Point?) {
    val url = "tmap://route?startx=${startLocation.longitude}&starty=${startLocation.latitude}&goalx=${endLocation?.longitude}&goaly=${endLocation?.latitude}&reqCoordType=WGS84&resCoordType=WGS84"

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

