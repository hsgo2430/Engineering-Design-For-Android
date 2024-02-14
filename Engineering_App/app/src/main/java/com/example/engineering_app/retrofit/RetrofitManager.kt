package com.example.engineering_app.retrofit

import android.content.ContentValues
import android.util.Log
import com.example.engineering_app.model.SleepyRestAreaResponse
import com.example.engineering_app.utils.API
import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat

class RetrofitManager {
    companion object{
        val instance = RetrofitManager()
    }
    // 레트로핏 인터페이스 가져옴
    private val iRetrofit: IRetrofit? = RetrofitClient.getClient(API.BASE_URL)?.create(IRetrofit::class.java)

    fun getSleepyRestArea(){
        iRetrofit?.getSleepyRestArea() // 인터페이스에서 사용할 메소드 선택
            ?.enqueue(object : retrofit2.Callback<JsonElement> {
                override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                    if (response.isSuccessful) {
                        // 정상적으로 통신이 성공된 경우
                        var result: JsonElement? = response.body()
                        Log.d("로그", "onResponse 성공: " + result?.toString());
                    } else {
                        // 통신이 실패한 경우
                        Log.d("로그", "onResponse 실패")
                    }
                }

                override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                    Log.d("로그", "onFailure")
                }
            })
    }
    // 사진 검색 API 호출
    /*fun searchPhotos(searchTerm: String?, completion: (RESPONSE_STATE, ArrayList<Photo>?) -> Unit){

        val term = searchTerm.let{
            it
        }?: "" // 입력된 searchTerm이 null일수 있으므로 string으로 변경

        val call = iRetrofit?.searchPhotos(searchTerm = term) ?: return // iRetrofit에 있는 함수에 인수로 searchTerm을 넣어줌

        call.enqueue(object : retrofit2.Callback<JsonElement>{

            //응답 성공시
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                Log.d(TAG, "응답 성공 / response : ${response.raw()}")

                when(response.code()){
                    200->{

                        response.body()?.let {// 응답의 몸체에서

                            var parsePhotoDataArray = ArrayList<Photo>()

                            val body = it.asJsonObject // 그 몸체를 JsonObject로 뽑아낸 뒤

                            val results = body.getAsJsonArray("results") // Object에서 result 부분만 빼서옴

                            val total = body.get("total").asInt

                            Log.d(TAG, "RetrofitManager - onResponse() called / total : $results")

                            results.forEach{
                                    resultItem ->
                                val resultItemObject = resultItem.asJsonObject

                                val user = resultItemObject.get("user").asJsonObject
                                val userName: String = user.get("username").asString
                                // 유저 이름을 가져오는 방식

                                val likeCount: Int = resultItemObject.get("likes").asInt
                                // 좋아요 수

                                val thumnailLink = resultItemObject.get("urls").asJsonObject.get("thumb").asString
                                // 썸네일

                                val createAt = resultItemObject.get("created_at").asString
                                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                                val formatter = SimpleDateFormat("yyyy년\nMM월 dd일")
                                val outputDataString = formatter.format(parser.parse(createAt))
                                // 생성일을 원하는 문자열로 변환

                                //Log.d(TAG, "RetrofitManager - outputDataString : $outputDataString")

                                val photoItem = Photo(
                                    author = userName,
                                    thumbnail = thumnailLink,
                                    likesCount = likeCount,
                                    createdAt = outputDataString
                                )

                                parsePhotoDataArray.add(photoItem)
                            }
                            completion(RESPONSE_STATE.OKAY, parsePhotoDataArray)
                        }
                    }
                }
            }

            //응답 실패시
            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                Log.d(ContentValues.TAG, "응답 실패 /t : $t")
                completion(RESPONSE_STATE.FALSE, null)
            }

        })
    }*/
}