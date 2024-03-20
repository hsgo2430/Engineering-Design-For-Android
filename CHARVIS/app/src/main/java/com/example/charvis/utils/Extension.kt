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

    /*fun checkBluetoothPermissionForAndroid11(context: Context) :Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }*/
}
