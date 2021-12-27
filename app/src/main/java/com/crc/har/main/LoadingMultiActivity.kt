package com.crc.har.main

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.crc.har.R
import com.crc.har.base.Constants
import com.crc.har.bluetooth.BluetoothLeService
import com.crc.har.bluetooth.SampleGattAttributes
import com.crc.har.gyro.GyroActivity
import com.crc.har.measure.HeartBeatMeasureActivity
import com.crc.har.pressure.PressureActivity
import com.crc.har.temperature.TemperatureActivity
import java.util.ArrayList

class LoadingMultiActivity : AppCompatActivity(), View.OnClickListener  {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mScanning: Boolean = false
    private var mHandler: Handler? = null

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    private var mDeviceName: String? = null
    private var mDeviceAddress: String? = null
    private var mGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? = ArrayList()
    private var mConnected = false
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null

    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"

    var nFunctionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_loading)

        var ivLoadingText : ImageView = findViewById(R.id.iv_loading_text)
        ivLoadingText.setOnClickListener(this)

        val intent = intent
        if(intent != null) {
            nFunctionIndex = intent.getIntExtra(Constants.SELECT_FUNCTION_INDEX, Constants.MAIN_FUNCTION_INDEX_STATISTICS)
        }

        mHandler = Handler()

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.str_bluetooth_not_supported, Toast.LENGTH_SHORT).show()
            return
        }

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

//        moveMainActivity()
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> when (grantResults) {
                intArrayOf(PackageManager.PERMISSION_GRANTED) -> {
                    Log.d("ScanDeviceActivity", "onRequestPermissionsResult(PERMISSION_GRANTED)")
//                    bluetoothLeScanner.startScan(bleScanner)
                }
                else -> {
                    Log.d("ScanDeviceActivity", "onRequestPermissionsResult(not PERMISSION_GRANTED)")
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()

        if (!mBluetoothAdapter!!.isEnabled) {
            if (!mBluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT)
            }
        }

        // Initializes list view adapter.

        scanLeDevice(true)

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mDeviceAddress)
            Log.d("eleutheria", "Connect request result=" + result)
        }
    }

    override fun onPause() {
        super.onPause()
        scanLeDevice(false)
        unregisterReceiver(mGattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(mServiceConnection)
        mBluetoothLeService = null

        mScanning = false
        bluetoothLeScanner.stopScan(mLeScanCallback)
    }

    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler!!.postDelayed(Runnable {
                mScanning = false

                bluetoothLeScanner.stopScan(mLeScanCallback)
            }, Constants.SCAN_PERIOD)

            mScanning = true
            bluetoothLeScanner.startScan(mLeScanCallback)
        } else {
            mScanning = false
            bluetoothLeScanner.stopScan(mLeScanCallback)
        }
//        mBluetoothAdapter!!.startDiscovery()

//        if (enable) {
//            // Stops scanning after a pre-defined scan period.
//            mHandler!!.postDelayed({
//                mScanning = false
//                mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
//                invalidateOptionsMenu()
//            }, Constants.SCAN_PERIOD)
//
//            mScanning = true
//            mBluetoothAdapter!!.startLeScan(mLeScanCallback)
//        } else {
//            mScanning = false
//            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
//        }
    }

    private val mLeScanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("eleutheria", "onScanFailed")
        }


        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            var strDeviceAddress = result!!.device.address
            Log.e("eleutheria", "address : ${strDeviceAddress}")

            when(nFunctionIndex) {
                Constants.MAIN_FUNCTION_INDEX_GYRO -> {
                    if(strDeviceAddress == Constants.MODULE_ADDRESS_GYRO) {
                        Log.e("eleutheria", "find device GYRO")
                        mBluetoothLeService!!.connect(strDeviceAddress)
                    }
                }
                Constants.MAIN_FUNCTION_INDEX_REAR -> {
                    if(strDeviceAddress == Constants.MODULE_ADDRESS_REAR) {
                        Log.e("eleutheria", "find device Rear")
                        mBluetoothLeService!!.connect(strDeviceAddress)
                    }
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.e("eleutheria", "onBatchScanResults")
        }
    }

    private val bluetoothLeScanner: BluetoothLeScanner
        get() {
            val bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            return bluetoothAdapter.bluetoothLeScanner
        }

    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            if (!mBluetoothLeService!!.initialize()) {
                Log.e("eleutheria", "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService!!.connect(mDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothLeService.ACTION_GATT_CONNECTED == action) {
                mConnected = true
                Log.d("eleutheria", "ACTION_GATT_CONNECTED")

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                mConnected = false
                Log.d("eleutheria", "ACTION_GATT_DISCONNECTED")
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
                addGattServices(mBluetoothLeService!!.supportedGattServices)
                activeNotification()
                Log.d("eleutheria", "ACTION_GATT_SERVICES_DISCOVERED")
                moveMainActivity()
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == action) {
                parsingData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
                Log.d("eleutheria", "ACTION_DATA_AVAILABLE")
            }
        }
    }

    private fun parsingData(data: String?) {
        if (data != null) {
//            Log.e("eleutheria", "data : ${data}")

            //System.out.println(data);
        }
    }

    private fun addGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String? = null
        val unknownServiceString = resources.getString(R.string.str_bluetooth_unknown_service)
        val unknownCharaString = resources.getString(R.string.str_bluetooth_unknown_characteristic)
        val gattServiceData = ArrayList<HashMap<String, String>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String>>>()
        mGattCharacteristics = ArrayList<ArrayList<BluetoothGattCharacteristic>>()

        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            Log.e("eleutheria", "uuid : " + uuid)
            println(uuid)
            currentServiceData.put(
                LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString)
            )
            currentServiceData.put(LIST_UUID, uuid)
            gattServiceData.add(currentServiceData)

            val gattCharacteristicGroupData = ArrayList<HashMap<String, String>>()
            val gattCharacteristics = gattService.characteristics
            val charas = ArrayList<BluetoothGattCharacteristic>()

            // Loops through available Characteristics.
            for (gattCharacteristic in gattCharacteristics) {
                charas.add(gattCharacteristic)
                val currentCharaData = HashMap<String, String>()
                uuid = gattCharacteristic.uuid.toString()
                println(uuid)
                println(currentCharaData)

                currentCharaData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString)
                )
                currentCharaData.put(LIST_UUID, uuid)
                gattCharacteristicGroupData.add(currentCharaData)
            }
            mGattCharacteristics!!.add(charas)
            gattCharacteristicData.add(gattCharacteristicGroupData)
        }
    }

    private fun activeNotification() {
        if (mGattCharacteristics != null) {
            val characteristic = mGattCharacteristics!![2][0]
            val charaProp = characteristic.properties
            if (charaProp or BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService!!.setCharacteristicNotification(
                        mNotifyCharacteristic!!, false)
                    mNotifyCharacteristic = null
                }
                mBluetoothLeService!!.readCharacteristic(characteristic)
            }
            if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                mNotifyCharacteristic = characteristic
                mBluetoothLeService!!.setCharacteristicNotification(
                    characteristic, true)
            }
        }
    }

    override fun onClick(v: View?) {
        scanLeDevice(true)
    }

    private fun moveMainActivity() {

        when(nFunctionIndex) {
            Constants.MAIN_FUNCTION_INDEX_GYRO -> {
                val intent = Intent(this, GyroActivity::class.java)
                startActivity(intent)
            }
            Constants.MAIN_FUNCTION_INDEX_REAR -> {
                val intent = Intent(this, GyroActivity::class.java)
                startActivity(intent)
            }
        }
    }

    companion object {
        private val TAG = LoadingMultiActivity::class.java!!.getSimpleName()


        var mBluetoothLeService: BluetoothLeService? = null

        private fun makeGattUpdateIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
            return intentFilter
        }
    }
}