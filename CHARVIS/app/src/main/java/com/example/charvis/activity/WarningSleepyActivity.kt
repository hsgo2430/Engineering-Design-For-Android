package com.example.charvis.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.example.charvis.R
import com.example.charvis.databinding.ActivityWarningSleepyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class WarningSleepyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWarningSleepyBinding
    private val myDynamicReceiver = MyDynamicReceiver()

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var warning: Boolean = true
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWarningSleepyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                changeScreenColor()
                handler.postDelayed(this, 500)
            }
        }

        handler.post(runnable)

    }

    private fun initView(){
        val filter = IntentFilter().apply {
            addAction("ACTION_RED_SCREEN")
            addAction("ACTION_WHITE_SCREEN")
        }
        registerReceiver(myDynamicReceiver, filter)

        sendBroadcast(Intent().apply {
            action = "ACTION_WHITE_SCREEN"
        })

        binding.turnOffAlarmBtn.setOnClickListener {
            if(mediaPlayer.isPlaying){
                mediaPlayer.stop()
                mediaPlayer.reset()
            }
            finish()
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.good_morning)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

    }

    private fun changeScreenColor(){
        if (warning){
            warning = !warning
            sendBroadcast(Intent().apply {
                action = "ACTION_RED_SCREEN"
            })
        }
        else{
            warning = !warning
            sendBroadcast(Intent().apply {
                action = "ACTION_WHITE_SCREEN"
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.reset()
        }
        handler.removeCallbacks(runnable)
    }

    inner class MyDynamicReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "ACTION_RED_SCREEN" -> {
                    binding.angryCharvisBackgorund.background = getDrawable(R.color.red)
                }
                "ACTION_WHITE_SCREEN" -> {
                    binding.angryCharvisBackgorund.background = getDrawable(R.color.white)
                }
                else -> {
                    Log.e("로그", "오류")
                }
            }
        }
    }
}