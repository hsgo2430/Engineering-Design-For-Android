package com.example.engineering_app.model

data class Message (
    val message: String,
    val sendBy: String
){
    val sendByMe: String = "me"
    val sendByBot: String = "bot"
}