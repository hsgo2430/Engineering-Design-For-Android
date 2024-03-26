package com.example.charvis.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.charvis.databinding.ActivityBluetoothBinding
import com.example.charvis.feature.ConnectedBluetoothThread
import com.example.charvis.feature.TTS
import com.example.charvis.feature.callAPI
import com.example.charvis.utils.Extension.showMessage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.UUID


class BluetoothActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothBinding
    private val myDynamicReceiver = MyDynamicReceiver()

    private var permission_list = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager.adapter
    }

    lateinit var mBluetoothDevice: BluetoothDevice
    lateinit var mBluetoothSocket: BluetoothSocket
    private lateinit var mBluetoothHandler: Handler
    var mThreadConnectedBluetooth: ConnectedBluetoothThread? = null

    private val adapter: ArrayList<Pair<String, String>> = ArrayList()
    private val pairing_device: ArrayList<String> = ArrayList()

    lateinit var textToSpeech: TTS

    val client: OkHttpClient = OkHttpClient()
    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
    // chatGPT를 위한 변수

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                showMessage("블루투스 활성화")
            } else if (it.resultCode == RESULT_CANCELED) {
                showMessage("취소")
            }
        }

    private val BT_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        const val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1
        const val BT_MESSAGE_READ = 2

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, permission_list, 1);

        initView()
        val filter = IntentFilter().apply {
            addAction("ACTION_BLUETOOTH_ON")
            addAction("ACTION_BLUETOOTH_OFF")
        }
        registerReceiver(myDynamicReceiver, filter)

        sendBroadcast(Intent().apply {
            action = "ACTION_BLUETOOTH_OFF"
        })

    }

    /*private fun initBluetoothStateReceiver() {
        Log.e("로그", "initBluetoothStateReceiver() 시작")
        val bluetoothStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.e("로그", "onReceive()시작")
                if ("BLUETOOTH_STATE_CHANGED" == intent?.action) {
                    val state = intent.getIntExtra("state", BluetoothAdapter.ERROR)
                    Log.e("로그", state.toString())
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            Log.e("로그", state.toString())
                        }
                        BluetoothAdapter.STATE_ON -> {
                            Log.e("로그", state.toString())
                        }
                        // 필요한 경우 다른 상태들도 처리
                    }
                }
            }
        }

        // 인텐트 필터를 생성하고, BLUETOOTH_STATE_CHANGED 액션을 추가합니다.
        val filter = IntentFilter("BLUETOOTH_STATE_CHANGED")
        // 리시버를 등록합니다.
        LocalBroadcastManager.getInstance(this).registerReceiver(bluetoothStateReceiver, filter)
    }*/

    inner class MyDynamicReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "ACTION_BLUETOOTH_ON" -> {
                    Log.e("로그", "ACTION_BLUETOOTH_ON")
                    binding.notConnectingCharvisImage.isGone = true
                    binding.notConnectingCharvisTv.isGone = true
                    binding.connectBtn.isGone = true

                    binding.connectingCharvisImage.isVisible = true
                    binding.connectingCharvisTv.isVisible = true
                    binding.closeSleepyAreaBtn.isVisible = true
                    binding.talkWithCharvisBtn.isVisible = true
                }
                "ACTION_BLUETOOTH_OFF" -> {
                    Log.e("로그", "ACTION_BLUETOOTH_OFF")
                    binding.notConnectingCharvisImage.isVisible = true
                    binding.notConnectingCharvisTv.isVisible = true
                    binding.connectBtn.isVisible = true

                    binding.connectingCharvisImage.isGone = true
                    binding.connectingCharvisTv.isGone = true
                    binding.closeSleepyAreaBtn.isGone = true
                    binding.talkWithCharvisBtn.isGone = true
                }
                else -> {
                    Log.e("로그", "오류")
                }
            }
        }
    }

    private fun initView() {

        textToSpeech = TTS(this, null)

        binding.connectBtn.setOnClickListener {
            getPairedDevices()
        }

        mBluetoothHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == BT_MESSAGE_READ) {
                    val readMessage = (msg.obj as? ByteArray)?.toString(Charset.forName("UTF-8"))
                    Log.e("로그", readMessage.toString())
                    callAPI(readMessage, client, JSON, textToSpeech)
                }
            }
        }

    }

    private fun bluetoothOn() {
        bluetoothAdapter?.let {
            if (!it.isEnabled) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activityResultLauncher.launch(intent)
            } else {
                Toast.makeText(this, "이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getPairedDevices() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) { // 지금 버전이 안드로이드 10보다 크면
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 부여되지 않았다면, 사용자에게 권한 요청
                requestPermissions(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH
                    ),
                    REQUEST_BLUETOOTH_CONNECT_PERMISSION
                )
            }
            else{
                bluetoothAdapter?.let {
                    if (it.isEnabled) {
                        val pairedDevices: Set<BluetoothDevice> = it.bondedDevices
                        if (pairedDevices.isNotEmpty()) {
                            adapter.clear()
                            pairing_device.clear()
                            pairedDevices.forEach { device ->
                                adapter.add(Pair(device.name, device.address))
                                pairing_device.add(device.name)
                            }
                            MaterialAlertDialogBuilder(this)
                                .setTitle("페어링할 기기를 선택해 주세요")
                                .setItems(pairing_device.toTypedArray()) { dialog, position -> connectSelectedDevice(pairing_device[position], pairedDevices) }
                                .show()

                        } else {
                            Toast.makeText(this, "페어링된 장치가 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        showMessage("블루투스")
                    }
                }
            }
        } // 안드로이드 버전 11 부터
        else {
            bluetoothAdapter?.let {
                if (it.isEnabled) {
                    val pairedDevices: Set<BluetoothDevice> = it.bondedDevices
                    Log.d("로그", it.bondedDevices.toString())
                    if (pairedDevices.isNotEmpty()) {
                        adapter.clear()
                        pairing_device.clear()
                        pairedDevices.forEach { device ->
                            adapter.add(Pair(device.name, device.address))
                            pairing_device.add(device.name)
                        }
                        MaterialAlertDialogBuilder(this)
                            .setTitle("페어링할 기기를 선택해 주세요")
                            .setItems(pairing_device.toTypedArray()) { dialog, position -> connectSelectedDevice(pairing_device[position], pairedDevices) }
                            .show()

                    } else {
                        Toast.makeText(this, "페어링된 장치가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showMessage("블루투스")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BLUETOOTH_CONNECT_PERMISSION -> {
                // 요청한 권한이 모두 부여되었는지 확인
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    showMessage("권한이 부여되었습니다.")
                } else {
                    showMessage("해당 기능을 지원하지 않습니다.")
                }
                return
            }
        }
    }

    private fun connectSelectedDevice(selected_device: String, pairing_device: Set<BluetoothDevice>){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            mBluetoothDevice = pairing_device.firstOrNull { device_list ->
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    showMessage("권한 부여 필요")
                    return
                }
                else{
                    selected_device == device_list.name
                }
            }!!
        }
        else{
            mBluetoothDevice = pairing_device.firstOrNull { device_list -> selected_device == device_list.name}!!
        }

        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID)
            mBluetoothSocket.connect()
            mThreadConnectedBluetooth = ConnectedBluetoothThread(mBluetoothSocket, mBluetoothHandler)
            mThreadConnectedBluetooth!!.start()
            sendBroadcast(Intent().apply {
                action = "ACTION_BLUETOOTH_ON"
            })
        }
        catch (e: IOException){
            showMessage("블루투스 연결 중 오류가 발생했습니다.")
        }
    }

    /*class ConnectedBluetoothThread(socket: BluetoothSocket, mBluetoothHandler: Handler) : Thread() {
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
                showMessage("에러")
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
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget()
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
                showMessage("데이터 전송 중 오류가 발생했습니다.")
            }
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                showMessage( "소켓 해제 중 오류가 발생했습니다.")
            }
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        mThreadConnectedBluetooth?.cancel()
        unregisterReceiver(myDynamicReceiver)
        textToSpeech.destroy()
    }

}