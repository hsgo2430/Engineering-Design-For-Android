package com.example.engineering_app.utils

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.engineering_app.App
import com.example.engineering_app.activity.BluetoothActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
