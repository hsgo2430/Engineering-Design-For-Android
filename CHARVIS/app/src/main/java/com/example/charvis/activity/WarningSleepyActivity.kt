package com.example.charvis.activity

import android.app.Activity
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
import kotlin.random.Random

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

        val intent: Intent = intent
        val gender = intent.getIntExtra("gender", -1)
        val child = intent.getBooleanExtra("child", false)
        val son = intent.getBooleanExtra("son", false)
        val daughter = intent.getBooleanExtra("daughter", false)
        val readMessage = intent.getStringExtra("readMessage")


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

            val returnIntent = Intent(this@WarningSleepyActivity, BluetoothActivity::class.java)
            returnIntent.putExtra("readMessage", readMessage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        mediaPlayer = setSound(child, gender, son, daughter)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

    }

    private fun setSound(
        child: Boolean,
        gender: Int,
        son: Boolean,
        daughter: Boolean
    ): MediaPlayer{
        lateinit var mediaPlayer: MediaPlayer

        if(child){
            if(gender == 1 || gender == 3){
                mediaPlayer = if(son && !daughter){
                    MediaPlayer.create(this, R.raw.son_to_dad)
                } else if(!son && daughter){
                    MediaPlayer.create(this, R.raw.daughter_to_dad)
                } else{
                    if(Random.nextBoolean()) MediaPlayer.create(this, R.raw.son_to_dad) else MediaPlayer.create(this, R.raw.daughter_to_dad)
                }
            } // 아버지 혹은 디폴트
            else{
                mediaPlayer = if(son && !daughter){
                    MediaPlayer.create(this, R.raw.son_to_mom)
                } else if(!son && daughter){
                    MediaPlayer.create(this, R.raw.daugter_to_mom)
                } else{
                    if(Random.nextBoolean()) MediaPlayer.create(this, R.raw.son_to_mom) else MediaPlayer.create(this, R.raw.daugter_to_mom)
                }
            } // 어머니
        } // 아이가 있을 때
        else{
            mediaPlayer = if(gender == 1 || gender == 3){
                if(Random.nextBoolean()) MediaPlayer.create(this, R.raw.dad_to_son) else MediaPlayer.create(this, R.raw.mon_to_son)
            } // 아버지 혹은 디폴트
            else{
                if(Random.nextBoolean()) MediaPlayer.create(this, R.raw.dad_to_daughter) else MediaPlayer.create(this, R.raw.mom_to_daughter)
            } // 어머니
        } // 아이가 없을때

        return mediaPlayer
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