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
import android.content.res.XmlResourceParser
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
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
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.charvis.App
import com.example.charvis.R
import com.example.charvis.databinding.ActivityBluetoothBinding
import com.example.charvis.feature.ConnectedBluetoothThread
import com.example.charvis.feature.TTS
import com.example.charvis.feature.callAPI
import com.example.charvis.feature.findNearestNeighbor
import com.example.charvis.feature.searchLoadToTMap
import com.example.charvis.model.LocationCallback
import com.example.charvis.model.Point
import com.example.charvis.model.User
import com.example.charvis.utils.Extension.showMessage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    private lateinit var uid: String
    private lateinit var databaseReference: DatabaseReference
    private var userdata: User = User()

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

    private lateinit var sleepyRestArea: ArrayList<Point>
    private lateinit var locationManager: LocationManager
    private lateinit var currentPosition: Point
    private var previousPosition: Point = Point("이전 위치", 0.00, 0.00)

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, permission_list, 1);

        initView()
        bluetoothOn()

        val filter = IntentFilter().apply {
            addAction("ACTION_BLUETOOTH_ON")
            addAction("ACTION_BLUETOOTH_OFF")
        }
        registerReceiver(myDynamicReceiver, filter)

        sendBroadcast(Intent().apply {
            action = "ACTION_BLUETOOTH_OFF"
        })

        sleepyRestArea = getXml()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                savePreviousPosition()  // 위치 저장 함수 호출
                handler.postDelayed(this, 30000)  // 다시 30초 후에 실행
            }
        }

        handler.post(runnable)

    }

    inner class MyDynamicReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                "ACTION_BLUETOOTH_ON" -> {
                    binding.notConnectingCharvisImage.isGone = true
                    binding.notConnectingCharvisTv.isGone = true
                    binding.connectBtn.isGone = true

                    binding.connectingCharvisImage.isVisible = true
                    binding.connectingCharvisTv.isVisible = true
                    binding.closeSleepyAreaBtn.isVisible = true
                    binding.talkWithCharvisBtn.isVisible = true
                }
                "ACTION_BLUETOOTH_OFF" -> {
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

        uid = intent.getStringExtra("uid").toString()

        getUserData(uid)

        textToSpeech = TTS(this, null)

        binding.connectBtn.setOnClickListener {
            getPairedDevices()
        }

        binding.talkWithCharvisBtn.setOnClickListener {
            callAPI(this.getString(R.string.want_to_talk_with_charvis) + "내가 관심사는 "+ userdata.interest + "이야", client, JSON, textToSpeech)
        }

        binding.closeSleepyAreaBtn.setOnClickListener {
            textToSpeech.speak("가까운 졸음 쉼터를 안내해 드릴게요.")
            getCurrentPosition(object : LocationCallback {
                override fun onLocationReceived(point: Point) {
                    val target2: Point = Point("임시", 36.9001423, 127.1889603) // 임시 데이터
                    val tmpPrevious: Point = Point("임시 기준점", 36.9033423, 127.1889603) // 임시 도착지

                    val nearestNeighbor = findNearestNeighbor(target2, tmpPrevious ,sleepyRestArea)
                    searchLoadToTMap(this@BluetoothActivity, target2, nearestNeighbor)
                }
            })
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
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                } else {

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

    private fun getXml():ArrayList<Point>{
        val parser: XmlResourceParser = resources.getXml(R.xml.sleepy_rest_area)
        val sleepyRestArea: ArrayList<Point> = ArrayList<Point>()
        val oneSleepyRestArea : ArrayList<String> = ArrayList<String>()
        var count = 0

        while (parser.eventType != XmlResourceParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlResourceParser.START_TAG -> {
                    val tagName = parser.name
                }
                XmlResourceParser.TEXT -> {
                    val text = parser.text
                    oneSleepyRestArea.add(text)
                    count++
                }
                XmlResourceParser.END_TAG -> {
                    val tagName = parser.name
                }
            }

            if(count == 3){
                count = 0
                sleepyRestArea.add(Point(oneSleepyRestArea[0], oneSleepyRestArea[1].toDouble(), oneSleepyRestArea[2].toDouble()))
                oneSleepyRestArea.clear()
            }
            parser.next()
        }
        return sleepyRestArea
    }
    private fun getUserData(uid: String){
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        //데이터 베이스 경로 저장
        databaseReference.child(uid).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val group: User? = snapshot.getValue(User::class.java)
                userdata = User(
                    nickname = group?.nickname.toString(),
                    emailId = group?.emailId.toString(),
                    password = group?.password.toString(),
                    idToken = group?.idToken.toString(),
                    interest = group?.interest.toString()
                )

                showMessage(userdata.nickname + "님 어서오세요!\n 관심사: " + userdata.interest)
            }

            override fun onCancelled(error: DatabaseError) {
                userdata = User(
                    nickname = "Null",
                    emailId = "Null",
                    password = "Null",
                    idToken = "Null",
                    interest = "Null"
                )
            }

        })
    }

    private fun savePreviousPosition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val previousPosition = Point("이전 위치", location.latitude, location.longitude)
                    Log.d("로그", previousPosition.toString())
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }, null)
        }
    }

    private fun getCurrentPosition(locationCallback: LocationCallback) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val currentPosition = Point("현재위치", location.latitude, location.longitude)
                    locationCallback.onLocationReceived(currentPosition) // 콜백 호출
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }, null)
        }
    }

    /*private fun getStartPosition(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Request location updates
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, object :
                LocationListener {
                override fun onLocationChanged(location: Location) {
                    current_position = Point(
                        "현재위치",
                        location.latitude,
                        location.longitude
                    )

                }

                override fun onStatusChanged(
                    provider: String?,
                    status: Int,
                    extras: Bundle?
                ) {
                }

                override fun onProviderEnabled(provider: String) {
                }

                override fun onProviderDisabled(provider: String) {
                }
            }, null)
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        mThreadConnectedBluetooth?.cancel()
        unregisterReceiver(myDynamicReceiver)
        textToSpeech.destroy()
        handler.removeCallbacks(runnable)
    }

}