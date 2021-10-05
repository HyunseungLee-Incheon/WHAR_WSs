package com.crc.har.bluetooth

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.crc.har.base.Constants
import java.util.*

class BluetoothLeService: Service() {

    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mConnectionState = STATE_DISCONNECTED

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED
                broadcastUpdate(intentAction)
                Log.i(TAG, "Connected to GATT server.")
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt!!.discoverServices())

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server.")
                disconnectGattServer()
                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status)
            }
            setMCNotification(true)
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic
        ) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }
    }

    fun disconnectGattServer() {
        Log.d("eleutheria", "Closing Gatt connection")
        // reset the connection flag
        mConnectionState = STATE_DISCONNECTED

        // disconnect and close the gatt
        if (mBluetoothGatt != null) {
            mBluetoothGatt!!.disconnect()
            mBluetoothGatt!!.close()
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String,
                                characteristic: BluetoothGattCharacteristic
    ) {
        val intent = Intent(action)

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
//        if (UUID.fromString(SampleGattAttributes.SERVICE_HEART_BEAT_MEASUREMENT) == characteristic.uuid) {
//            val flag = characteristic.properties
//            var format = -1
//            if (flag and 0x01 != 0) {
//                format = BluetoothGattCharacteristic.FORMAT_UINT16
//                Log.d(TAG, "Heart rate format UINT16.")
//            } else {
//                format = BluetoothGattCharacteristic.FORMAT_UINT8
//                Log.d(TAG, "Heart rate format UINT8.")
//            }
////            val heartRate = characteristic.getIntValue(format, 1)!!
//            when(Constants.nCurFunctionIndex) {
//                Constants.MAIN_FUNCTION_INDEX_UV -> {
//
//                    Log.d("eleutheria", String.format("Received UV string : ${characteristic.getStringValue(1)}"))
//
//                    var strReceivedValue = characteristic.getStringValue(1)
//                    val arReceivedValue = strReceivedValue.split(".")
//                    if(arReceivedValue[4].contains("U")) {
//                        val strDivideValue = arReceivedValue[4].substring(1)
//                        val nUVSignal = strDivideValue.toInt()
//                        Log.d("eleutheria", String.format("nUVSignal : $nUVSignal"))
//                        intent.putExtra(EXTRA_DATA, nUVSignal.toString())
//
//                        sendMessageToActivity(nUVSignal.toString())
//                    }
////                    val nUVSignal = Character.getNumericValue(characteristic.getIntValue(format, 1))
////                    Log.d("eleutheria", String.format("Received UV : %d", nUVSignal))
////                    intent.putExtra(EXTRA_DATA, nUVSignal.toString())
////
////                    sendMessageToActivity(nUVSignal.toString())
//
//                }
//                Constants.MAIN_FUNCTION_INDEX_GYRO -> {
//                    val nGyroSignal = Character.getNumericValue(characteristic.getIntValue(format, 1))
//                    Log.d("eleutheria", String.format("Received Gyro : %d", nGyroSignal))
//                    intent.putExtra(EXTRA_DATA, nGyroSignal.toString())
//
//                    if(nGyroSignal == Constants.ACTION_GYRO_SIGNAL) {
//                        sendMessageToActivity(nGyroSignal.toString())
//                    }
//                }
//            }
//        }

        // For all other profiles, writes the data formatted in HEX.
        val data = characteristic.value
        if (data != null && data.size > 0) {
            val stringBuilder = StringBuilder(data.size)
            for (byteChar in data)
                stringBuilder.append(String.format("%02X ", byteChar))
//            intent.putExtra(EXTRA_DATA, String(data) + "\n" + stringBuilder.toString())
            Log.e("eleutheria", "receive : ${String(data)}")

            val strReceiveData = String(data)
            when (Constants.nCurFunctionIndex) {
                Constants.MAIN_FUNCTION_INDEX_HB -> {
                    val subData = strReceiveData.substring(2, strReceiveData.length - 1)
                    Log.d("eleutheria", String.format("Received HB : ${subData}"))

                    sendMessageToActivity(subData)
                }
                Constants.MAIN_FUNCTION_INDEX_PRESSURE -> {
                    val subData = strReceiveData.substring(2, strReceiveData.length - 1)
                    Log.d("eleutheria", String.format("Received Pressure : ${subData}"))

                    sendMessageToActivity(subData)
                }
                Constants.MAIN_FUNCTION_INDEX_GYRO -> {
                    val subData = strReceiveData.substring(1, 2)
                    Log.d("eleutheria", String.format("Received Gyro : ${subData}"))

                    if(subData == Constants.ACTION_GYRO_SIGNAL) {
                        sendMessageToActivity(subData)
                    }
                }
                Constants.MAIN_FUNCTION_INDEX_TEMPERATURE -> {
                    if(strReceiveData.contains(Constants.RECIEVE_DATA_PREFIX_TEMPERATURE)) {
                        val subData = strReceiveData.substring(2, strReceiveData.length - 1)
                        Log.d("eleutheria", String.format("Received Temperature : ${subData}"))

                        sendMessageToActivity(subData)
                    }
                }
            }
        }

        sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        internal val service: BluetoothLeService
            get() = this@BluetoothLeService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
        return super.onUnbind(intent)
    }

    private val mBinder = LocalBinder()

    /**
     * Initializes a reference to the local Bluetooth adapter.

     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }

        mBluetoothAdapter = mBluetoothManager!!.adapter
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.

     * @param address The device address of the destination device.
     * *
     * *
     * @return Return true if the connection is initiated successfully. The connection result
     * *         is reported asynchronously through the
     * *         `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * *         callback.
     */
    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress
            && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")
            if (mBluetoothGatt!!.connect()) {
                mConnectionState = STATE_CONNECTING
                return true
            } else {
                return false
            }
        }

        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.disconnect()
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.

     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }

    fun writeCharacteristic(value: String) {
        Log.e("eleutheria", "send heartbeatCharacteristic : $value")
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        /*check if the service is available on the device*/
        val mCustomService =
            mBluetoothGatt!!.
            getService(UUID.fromString(SampleGattAttributes.SERVICE_HEART_BEAT_MEASUREMENT))
        if (mCustomService == null) {
            Log.w(TAG, "Custom BLE Service not found")
            return
        }
        /*get the read characteristic from the service*/
        val mWriteCharacteristic =
            mCustomService.getCharacteristic(
                UUID.
            fromString(SampleGattAttributes.CHARACTERISTIC_TEMPERATURE_MEASUREMENT))
//        mWriteCharacteristic.value = BigInteger(value, 16).toByteArray()
        val strValue : String = value
        mWriteCharacteristic.value = strValue.toByteArray()
        if (!mBluetoothGatt!!.writeCharacteristic(mWriteCharacteristic)) {
            Log.e("eleutheria", "send fail heartbeatCharacteristic : $value")
        }
    }

    /**
     * Enables or disables notification on a give characteristic.

     * @param characteristic Characteristic to act on.
     * *
     * @param enabled If true, enable notification.  False otherwise.
     */
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic,
                                      enabled: Boolean) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)

//        // This is specific to Heart Rate Measurement.
//        if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
//            val descriptor = characteristic.getDescriptor(
//                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG))
//            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//            mBluetoothGatt!!.writeDescriptor(descriptor)
//        }
    }

    fun setMCNotification(enabled: Boolean) {

        var uuidMCCharateristic = SampleGattAttributes.CHARACTERISTIC_HEART_BEAT_MEASUREMENT
        var uuidMCService = SampleGattAttributes.SERVICE_HEART_BEAT_MEASUREMENT

        when(Constants.nCurFunctionIndex) {
            Constants.MAIN_FUNCTION_INDEX_HB -> {
                uuidMCCharateristic = SampleGattAttributes.CHARACTERISTIC_HEART_BEAT_MEASUREMENT
                uuidMCService = SampleGattAttributes.SERVICE_HEART_BEAT_MEASUREMENT
            }
            Constants.MAIN_FUNCTION_INDEX_PRESSURE -> {
                uuidMCCharateristic = SampleGattAttributes.CHARACTERISTIC_PRESSURE_MEASUREMENT
                uuidMCService = SampleGattAttributes.SERVICE_PRESSURE_MEASUREMENT
            }
            Constants.MAIN_FUNCTION_INDEX_GYRO -> {
                uuidMCCharateristic = SampleGattAttributes.CHARACTERISTIC_GYRO_MEASUREMENT
                uuidMCService = SampleGattAttributes.SERVICE_GYRO_MEASUREMENT
            }
            Constants.MAIN_FUNCTION_INDEX_TEMPERATURE -> {
                uuidMCCharateristic = SampleGattAttributes.CHARACTERISTIC_TEMPERATURE_MEASUREMENT
                uuidMCService = SampleGattAttributes.SERVICE_TEMPERATURE_MEASUREMENT
            }
        }

        var mBluetoothLeService: BluetoothGattService? = null
        var mBluetoothGattCharacteristic: BluetoothGattCharacteristic? = null

        for(service: BluetoothGattService in mBluetoothGatt!!.getServices()) {
            if((service==null)||(service.uuid==null)) continue
            if(uuidMCService.equals(service.uuid.toString(), true)) {
                mBluetoothLeService = service
            }
        }
        if(mBluetoothLeService != null) {
            mBluetoothGattCharacteristic = mBluetoothLeService.getCharacteristic(UUID.fromString(uuidMCCharateristic))
        } else {
            Log.e("eleuthria", "mBluetoothLeService is null")
        }

        if(mBluetoothGattCharacteristic != null) {
            setCharacteristicNotification(mBluetoothGattCharacteristic, enabled)
        }
    }

    private fun sendMessageToActivity(msg: String) {
        var strActionName = "NoAction"

        when(Constants.nCurFunctionIndex) {
            Constants.MAIN_FUNCTION_INDEX_HB -> {
                strActionName = Constants.MESSAGE_SEND_HB
            }
            Constants.MAIN_FUNCTION_INDEX_PRESSURE -> {
                strActionName = Constants.MESSAGE_SEND_PRESSURE
            }
            Constants.MAIN_FUNCTION_INDEX_GYRO -> {
                strActionName = Constants.MESSAGE_SEND_GYRO
            }
            Constants.MAIN_FUNCTION_INDEX_TEMPERATURE -> {
                strActionName = Constants.MESSAGE_SEND_TEMPERATURE
            }
        }

        val intent = Intent(strActionName)
        // You can also include some extra data.
        intent.putExtra("value", msg)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.

     * @return A `List` of supported services.
     */
    val supportedGattServices: List<BluetoothGattService>?
        get() {
            if (mBluetoothGatt == null) return null

            return mBluetoothGatt!!.services
        }

    companion object {
        private val TAG = BluetoothLeService::class.java.simpleName

        private val STATE_DISCONNECTED = 0
        private val STATE_CONNECTING = 1
        private val STATE_CONNECTED = 2

        val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

    }
}