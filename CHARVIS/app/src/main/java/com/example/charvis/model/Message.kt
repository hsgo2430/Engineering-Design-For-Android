package com.example.charvis.model

data class Message (
    val message: String,
    val sendBy: String
){
    val sendByMe: String = "me"
    val sendByBot: String = "bot"
}