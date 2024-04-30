package com.example.charvis.feature

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.charvis.activity.BluetoothActivity
import com.example.charvis.utils.Extension
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class ConnectedBluetoothThread(socket: BluetoothSocket, mBluetoothHandler: Handler) : Thread() {
    private val mmSocket: BluetoothSocket = socket
    private val mmInStream: InputStream?
    private val mmOutStream: OutputStream?
    private val mBluetoothHandler = mBluetoothHandler
        init {
        var tmpIn: InputStream? = null
        var tmpOut: OutputStream? = null

        try {
            tmpIn = socket.inputStream
            tmpOut = socket.outputStream
        } catch (e: IOException) {
            Extension.showMessage("에러")
        }

        mmInStream = tmpIn
        mmOutStream = tmpOut
    }

    override fun run() {
        val buffer = ByteArray(1024)
        var bytes: Int

        while (true) {
            try {
                bytes = mmInStream?.available() ?: 0
                if (bytes != 0) {
                    buffer.fill(0)
                    SystemClock.sleep(100)
                    bytes = mmInStream?.available() ?: 0
                    bytes = mmInStream?.read(buffer, 0, bytes) ?: 0
                    if (bytes > 0) {
                        val receivedString = buffer.copyOfRange(0, bytes).toString(Charsets.UTF_8)
                        Log.d("로그 블루투스", receivedString)
                        mBluetoothHandler.obtainMessage(BluetoothActivity.BT_MESSAGE_READ, bytes, -1, receivedString).sendToTarget()
                    }
                }
            } catch (e: IOException) {
                break
            }
        }
    }

    fun write(str: String) {
        val bytes = str.toByteArray()
        try {
            mmOutStream?.write(bytes)
        } catch (e: IOException) {
            Extension.showMessage("데이터 전송 중 오류가 발생했습니다.")
        }
    }

    fun cancel() {
        try {
            mmSocket.close()
        } catch (e: IOException) {
            Extension.showMessage("소켓 해제 중 오류가 발생했습니다.")
        }
    }
}

/*class BluetoothStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (BluetoothAdapter.ACTION_STATE_CHANGED == intent.action) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            Log.e("로그", state.toString())
            Intent("BLUETOOTH_STATE_CHANGED").also { broadcastIntent ->
                broadcastIntent.putExtra("state", state)
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent)
            }
        }
    }
}*/

