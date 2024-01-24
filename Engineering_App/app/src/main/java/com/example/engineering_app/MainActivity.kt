package com.example.engineering_app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.engineering_app.adapter.MessageAdapter
import com.example.engineering_app.databinding.ActivityMainBinding
import com.example.engineering_app.model.Message
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    val message: Message = Message("tmp", "tmp")
    private var messageList: ArrayList<Message> = ArrayList<Message>()
    val client: OkHttpClient = OkHttpClient()
    private val messageAdapter: MessageAdapter = MessageAdapter(messageList)

    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

    private val mySecretKey: String = "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"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding.recyclerView.apply {
            setHasFixedSize(true)
            this.layoutManager = layoutManager
            this.adapter = messageAdapter
        }

        binding.btnSend.setOnClickListener{
            val question: String = binding.etMsg.text.toString()
            addToChat(question, message.sendByMe)
            binding.etMsg.text = null
            callAPI(question)
            binding.tvWelcome.visibility = View.GONE
        }

        client.newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private fun addToChat(message: String?, sentBy: String?) {
        runOnUiThread {
            messageList.add(Message(message!!, sentBy!!))
            messageAdapter.notifyDataSetChanged()
            binding.recyclerView.smoothScrollToPosition(messageAdapter.itemCount)
        }
    }

    fun addResponse(response: String){
        val message: Message = Message("tmp", "tmp")
        messageList.removeAt(messageList.size - 1)
        addToChat(response, message.sendByBot)
    }

    fun callAPI(question: String?) {
        //okhttp
        val message: Message = Message("tmp", "tmp")
        messageList.add(Message("...", message.sendByBot))

        //추가된 내용
        val arr = JSONArray()
        val baseAi = JSONObject()
        val userMsg = JSONObject()
        try {
            //AI 속성설정
            baseAi.put("role", "user")
            baseAi.put("content", "You are a helpful and kind AI Assistant.")
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
                addResponse("Failed to load response due to " + e.message)
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
                        addResponse(result.trim { it <= ' ' })
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body!!.string())
                }
            }
        })
    }
}