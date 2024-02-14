package com.example.engineering_app.retrofit

import com.example.engineering_app.utils.API
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET

interface IRetrofit {
    @GET(API.SLEEPY_REST_AREA)
    fun getSleepyRestArea(): Call<JsonElement>
}