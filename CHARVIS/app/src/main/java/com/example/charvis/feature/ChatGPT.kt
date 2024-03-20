package com.example.charvis.feature

import android.util.Log
import com.example.charvis.utils.Extension.showMessage
import com.example.engineering_app.model.Message
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException



fun callAPI(question: String?, client: OkHttpClient, JSON: MediaType, tts: TTS) {
    val mySecretKey: String = ""

    //okhttp
    val message: Message = Message("tmp", "tmp")

    //추가된 내용
    val arr = JSONArray()
    val baseAi = JSONObject()
    val userMsg = JSONObject()
    try {
        //AI 속성설정
        baseAi.put("role", "user")
        baseAi.put("content", "You are an AI Assistant.")
        //유저 메세지
        userMsg.put("role", "user")
        userMsg.put("content", question)
        //array로 담아서 한번에 보낸다
        arr.put(baseAi)
        arr.put(userMsg)
    } catch (e: JSONException) {
        throw RuntimeException(e)
    }
    val jsonObject = JSONObject()
    try {
        //모델명 변경
        jsonObject.put("model", "gpt-3.5-turbo")
        jsonObject.put("messages", arr)

    } catch (e: JSONException) {
        e.printStackTrace()
    }

    val body: RequestBody = jsonObject.toString().toRequestBody(JSON)
    val request: Request = Request.Builder()
        .url("https://api.openai.com/v1/chat/completions") //url 경로 수정됨
        .header("Authorization", "Bearer $mySecretKey")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("로그","Failed to load response due to " + e.message)
            tts.speak("에러가 발생했습니다")
        }

        @Throws(IOException::class)
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                var jsonObject: JSONObject? = null
                try {
                    jsonObject = JSONObject(response.body!!.string())
                    val jsonArray = jsonObject.getJSONArray("choices")
                    val result =
                        jsonArray.getJSONObject(0).getJSONObject("message").getString("content")
                    tts.speak(result)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                Log.e("로그","Failed to load response due to " + response.body!!.string())
                tts.speak("에러가 발생했습니다")
            }
        }
    })
}