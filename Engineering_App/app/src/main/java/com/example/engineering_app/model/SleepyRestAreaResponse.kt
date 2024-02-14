package com.example.engineering_app.model

import com.google.gson.annotations.SerializedName

data class SleepyRestAreaResponse (
    @SerializedName("shltrNm")
    val shltrNm:String, //졸음쉼터명
    @SerializedName("ctprvnNm")
    val ctprvnNm:String, //시도명
    @SerializedName("signguNm")
    val signguNm:String, //시군구명
    @SerializedName("roadKnd")
    val roadKnd:String, //도로종류
    @SerializedName("roadRouteNm")
    val roadRouteNm:String, //도로노선명
    @SerializedName("roadRouteNo")
    val roadRouteNo:String, //도로노선번호
    @SerializedName("roadRouteDrc")
    val roadRouteDrc:String, //도로노선방향
    @SerializedName("rdnmadr")
    val rdnmadr:String, //소재지도로명주소
    @SerializedName("lnmadr")
    val lnmadr:String, //소재지지번주소
    @SerializedName("latitude")
    val latitude: Double,//위도
    @SerializedName("longitude")
    val longitude: Double,//경도
    @SerializedName("totEt")
    val totEt:String, //총연장
    @SerializedName("prkplceCo")
    val prkplceCo:String, //주차면수
    @SerializedName("toiletYn")
    val toiletYn:String, //화장실유무
    @SerializedName("cctvCo")
    val cctvCo:String, //방범용CCTV수
    @SerializedName("etcCvntl")
    val etcCvntl:String, //기타편의시설
    @SerializedName("institutionNm")
    val institutionNm:String, //관리기관명
    @SerializedName("phoneNumber")
    val phoneNumber:String, //관리기관전화번호
    @SerializedName("referenceDate")
    val referenceDate:String, //데이터기준일자
    @SerializedName("instt_code")
    val instt_code:String,//제공기관코드
)