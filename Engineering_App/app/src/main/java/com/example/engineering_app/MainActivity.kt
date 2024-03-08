package com.example.engineering_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.engineering_app.activity.BluetoothActivity
import com.example.engineering_app.activity.ChatBotActivity
import com.example.engineering_app.activity.KaKaoMapActivity
import com.example.engineering_app.activity.NavigationActivity
import com.example.engineering_app.databinding.ActivityMainBinding


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

        binding.goToBluetooth.setOnClickListener{
            val intent = Intent(this, BluetoothActivity::class.java)
            startActivity(intent)
        }
    }
}