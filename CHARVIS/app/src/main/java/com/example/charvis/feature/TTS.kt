package com.example.charvis.feature

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TTS(context: Context, listener: OnInitListener?) : TextToSpeech(context, listener) {

    init {
        setPitch(1.0f) // 음성의 높낮이 설정
        setSpeechRate(1.0f) // 음성의 속도 설정
        language = Locale.KOREA
    }

    fun speak(text: CharSequence) {
        speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1")
    }

    fun destroy() {
        stop()
        shutdown()
    }
}