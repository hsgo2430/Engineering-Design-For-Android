package com.example.engineering_app.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.engineering_app.databinding.ActivityBluetoothBinding
import com.example.engineering_app.utils.Extension.showMessage
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

    private val adapter: ArrayList<Pair<String, String>> = ArrayList()

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                showMessage(this, "블루투스 활성화")
            } else if (it.resultCode == RESULT_CANCELED) {
                showMessage(this, "취소")
            }
        }

    private val BT_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, permission_list, 1);

        initView()
    }

    private fun initView() {
        binding.bluetoothOnBtn.setOnClickListener {
            bluetoothOn()
        }

        binding.connectBtn.setOnClickListener {
            getPairedDevices()
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
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 부여되지 않았다면, 사용자에게 권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_BLUETOOTH_CONNECT_PERMISSION
                )
                showMessage(this, "권한 부여 필요")
            }
            else{
                bluetoothAdapter?.let {
                    if (it.isEnabled) {
                        val pairedDevices: Set<BluetoothDevice> = it.bondedDevices
                        Log.d("로그", it.bondedDevices.toString())
                        if (pairedDevices.isNotEmpty()) {
                            adapter.clear()
                            pairedDevices.forEach { device ->
                                adapter.add(Pair(device.name, device.address))
                            }
                            showMessage(this, adapter.toString())
                        } else {
                            Toast.makeText(this, "페어링된 장치가 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        showMessage(this, "블루투스")
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
                        pairedDevices.forEach { device ->
                            adapter.add(Pair(device.name, device.address))
                        }
                        showMessage(this, adapter.toString())
                    } else {
                        Toast.makeText(this, "페어링된 장치가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showMessage(this, "블루투스")
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
                    // 권한이 부여되었다면, 여기에 블루투스 연결 로직을 추가
                } else {
                    // 권한이 거부되었다면, 사용자에게 권한이 필요한 이유를 설명하거나
                    // 기능을 사용할 수 없음을 알리는 로직을 추가
                }
                return
            }
        }
    }
}