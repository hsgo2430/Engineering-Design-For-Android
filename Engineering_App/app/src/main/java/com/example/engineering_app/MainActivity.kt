package com.example.engineering_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.engineering_app.activity.ChatBotActivity
import com.example.engineering_app.activity.KaKaoMapActivity
import com.example.engineering_app.activity.NavigationActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView(){
        binding.goToChatBotActivity.setOnClickListener {
            val intent = Intent(this, ChatBotActivity::class.java)
            startActivity(intent)
        }
        binding.goToNavigationActivity.setOnClickListener {
            val intent = Intent(this, NavigationActivity::class.java)
            startActivity(intent)
        }
        binding.goToKakaoMapActivity.setOnClickListener {
            val intent = Intent(this, KaKaoMapActivity::class.java)
            startActivity(intent)
        }
    }
}