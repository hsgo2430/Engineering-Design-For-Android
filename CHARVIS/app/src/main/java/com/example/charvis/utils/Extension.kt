package com.example.charvis.utils

import android.widget.Toast
import com.example.charvis.App

object Extension {
    fun String?.isJsonObject(): Boolean = this?.startsWith("{") == true && this.endsWith("}")

    fun String?.isJsonArray(): Boolean {
        return this?.startsWith("[") == true && this.endsWith("]")
    }

    fun showMessage(message: String){
        Toast.makeText(App.instance, message, Toast.LENGTH_LONG).show()
    }

    fun String.isEmail(): Boolean {
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return matches(emailRegex)
    }
}
