package com.example.engineering_app.utils

object Extension {
    fun String?.isJsonObject(): Boolean = this?.startsWith("{") == true && this.endsWith("}")

    fun String?.isJsonArray(): Boolean {
        return this?.startsWith("[") == true && this.endsWith("]")
    }
}