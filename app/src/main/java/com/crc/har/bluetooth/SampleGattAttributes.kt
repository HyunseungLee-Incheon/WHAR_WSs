package com.crc.har.bluetooth

import com.crc.har.base.Constants
import java.util.HashMap

object SampleGattAttributes {
    var attributes: HashMap<String, String> = HashMap()
    var CLIENT_CHARACTERISTIC_CONFIG =              "00002902-0000-1000-8000-00805f9b34fb"
    var HEART_RATE_AND_BATTERY_MEASUREMENT =        "0000ffe0-0000-1000-8000-00805f9b34fb"

    // service
    var SERVICE_HEART_BEAT_MEASUREMENT                              = Constants.MODULE_SERVICE_UUID_HB
    var SERVICE_PRESSURE_MEASUREMENT                                = Constants.MODULE_SERVICE_UUID_PRESSURE
    var SERVICE_GYRO_MEASUREMENT                                    = Constants.MODULE_SERVICE_UUID_GYRO
    var SERVICE_REAR_MEASUREMENT                                    = Constants.MODULE_SERVICE_UUID_REAR
    var SERVICE_TEMPERATURE_MEASUREMENT                             = Constants.MODULE_SERVICE_UUID_TEMPERATURE



    //characteristic
    var CHARACTERISTIC_HEART_BEAT_MEASUREMENT                         = Constants.MODULE_CHARACTERISTIC_UUID_HB
    var CHARACTERISTIC_PRESSURE_MEASUREMENT                           = Constants.MODULE_CHARACTERISTIC_UUID_PRESSURE
    var CHARACTERISTIC_GYRO_MEASUREMENT                               = Constants.MODULE_CHARACTERISTIC_UUID_GYRO
    var CHARACTERISTIC_REAR_MEASUREMENT                               = Constants.MODULE_CHARACTERISTIC_UUID_REAR
    var CHARACTERISTIC_TEMPERATURE_MEASUREMENT                        = Constants.MODULE_CHARACTERISTIC_UUID_TEMPERATURE


    init {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service")
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service")
        // Sample Characteristics.
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String")


        // Using unknown GATT profile, must debug other end
        attributes.put("19B10000-E8F2-537E-4F6C-D104768A1214", "ioTank")
        attributes.put(SERVICE_HEART_BEAT_MEASUREMENT, "Heart Rate Measurement")
        attributes.put(SERVICE_PRESSURE_MEASUREMENT, "Pressure Measurement")
        attributes.put(SERVICE_GYRO_MEASUREMENT, "GYRO Measurement")
        attributes.put(SERVICE_REAR_MEASUREMENT, "REAR Measurement")
        attributes.put(SERVICE_TEMPERATURE_MEASUREMENT, "Temperature Measurement")
    }


    fun lookup(uuid: String, defaultName: String): String {
        val name = attributes.get(uuid)
        return name ?: defaultName
    }
}
