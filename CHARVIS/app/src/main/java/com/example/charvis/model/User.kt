package com.example.charvis.model

data class User (
    var nickname: String = "",
    var emailId: String = "",
    var password: String = "",
    var idToken: String = "",
    var interest: String = "",
    var gender: Int = -1,
    var child: Boolean = false,
    var son: Boolean = false,
    var daughter: Boolean = false
)