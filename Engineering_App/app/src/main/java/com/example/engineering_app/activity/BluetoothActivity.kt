package com.example.engineering_app.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.engineering_app.databinding.ActivityBluetoothBinding
import com.example.engineering_app.tts.MyTTS
import com.example.engineering_app.utils.Extension.showMessage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.UUID


class BluetoothActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBluetoothBinding

    var permission_list = arrayOf(
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
    private var acceptThread: AcceptThread? = null

    private val adapter: ArrayList<Pair<String, String>> = ArrayList()
    private val pairing_device: ArrayList<String> = ArrayList()

    lateinit var textToSpeech: MyTTS

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
        private const val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1
        private const val BT_MESSAGE_READ = 2

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, permission_list, 1);

        initView()
    }

    private fun initView() {

        textToSpeech = MyTTS(this, null)

        binding.bluetoothOnBtn.setOnClickListener {
            bluetoothOn()
        }

        binding.sendDataBtn.setOnClickListener {
            if(mThreadConnectedBluetooth != null){
                mThreadConnectedBluetooth!!.write(binding.insertSendDataTv.text.toString())
                binding.insertSendDataTv.text.clear()
            }
            else{
                showMessage("mThreadConnectedBluetooth is null")
            }
        }

        binding.levelOneTv.setOnClickListener {
            if(mThreadConnectedBluetooth != null){
                mThreadConnectedBluetooth!!.write("1|0|졸음 레벨은 1입니다.")
                binding.insertSendDataTv.text.clear()
            }
            else{
                showMessage("mThreadConnectedBluetooth is null")
            }
        }

        binding.levelTwoTv.setOnClickListener {
            if(mThreadConnectedBluetooth != null){
                mThreadConnectedBluetooth!!.write("2|0|졸음 레벨은 2입니다.")
                binding.insertSendDataTv.text.clear()
            }
            else{
                showMessage("mThreadConnectedBluetooth is null")
            }
        }

        binding.levelThreeTv.setOnClickListener {
            if(mThreadConnectedBluetooth != null){
                mThreadConnectedBluetooth!!.write("3|1|졸음 레벨은 3입니다.")
                binding.insertSendDataTv.text.clear()
            }
            else{
                showMessage("mThreadConnectedBluetooth is null")
            }
        }

        binding.levelFourTv.setOnClickListener {
            if(mThreadConnectedBluetooth != null){
                mThreadConnectedBluetooth!!.write("4|0|졸음 레벨은 4입니다.")
                binding.insertSendDataTv.text.clear()
            }
            else{
                showMessage("mThreadConnectedBluetooth is null")
            }
        }

        binding.bluetoothServerBtn.setOnClickListener {
            startServer()
        }

        mBluetoothHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == BT_MESSAGE_READ) {
                    val readMessage = msg.obj.toString().split("|")
                    if(readMessage[0] == "0"){
                        binding.receiveDataTv.text = "유저 정보: " + readMessage[1]+ ", " + readMessage [2]
                    }
                    else{
                        if(mThreadConnectedBluetooth != null){
                            mThreadConnectedBluetooth!!.write("0|0|0")
                        }
                        else{
                            showMessage("mThreadConnectedBluetooth is null")
                        }
                    }
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
        }
        catch (e: IOException){
            showMessage("블루투스 연결 중 오류가 발생했습니다.")
        }
    }

    private fun startServer() {
        val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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
            bluetoothAdapter?.listenUsingRfcommWithServiceRecord("MyApp", BT_UUID)
        }
        acceptThread = AcceptThread(serverSocket)
        acceptThread?.start()
        showMessage("서버 시작: 클라이언트의 연결을 기다립니다...")
    }

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
    }

    inner class AcceptThread(serverSocket: BluetoothServerSocket?) : Thread() {

        val serverSocket = serverSocket

        override fun run() {
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    serverSocket?.accept()
                } catch (e: IOException) {
                    Log.e("AcceptThread", "소켓의 accept() 메서드 실패", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    manageMyConnectedSocket(it)
                    serverSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // 클라이언트와 연결된 후의 처리
        private fun manageMyConnectedSocket(socket: BluetoothSocket) {
            mThreadConnectedBluetooth = ConnectedBluetoothThread(socket, mBluetoothHandler).apply {
                start()
            }
            runOnUiThread {
                showMessage("클라이언트와 연결되었습니다.")
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e("AcceptThread", "서버 소켓을 닫는데 실패", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        acceptThread?.cancel()
        mThreadConnectedBluetooth?.cancel()
        textToSpeech.destroy()
    }

}