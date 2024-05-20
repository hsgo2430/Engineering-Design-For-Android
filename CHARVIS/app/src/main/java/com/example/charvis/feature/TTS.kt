package com.example.charvis.feature

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TTS(context: Context, listener: OnInitListener?, mThreadConnectedBluetooth: ConnectedBluetoothThread) : TextToSpeech(context, listener) {

    init {
        setPitch(1.0f) // 음성의 높낮이 설정
        setSpeechRate(1.3f) // 음성의 속도 설정
        language = Locale.KOREA

        setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // 음성 출력이 시작될 때 호출됩니다.
            }

            override fun onDone(utteranceId: String?) {
                mThreadConnectedBluetooth!!.write("0")
            }

            override fun onError(utteranceId: String?) {
                // 오류가 발생했을 때 호출됩니다.
            }
        })
    }

    fun speak(text: CharSequence) {
        speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1")
    }

    fun destroy() {
        stop()
        shutdown()
    }
}