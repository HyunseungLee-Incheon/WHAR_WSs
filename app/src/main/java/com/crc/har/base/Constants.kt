package com.crc.har.base

import com.crc.har.BuildConfig

class Constants {
    companion object {
        var strDeviceName = "Not Connected"
        var strDeviceAddress = "Not Connected"
        var strEmergencyNumber : String = "119"
        val SPLASH_LOADING_TIME : Long = 3000
        val SPLASH_WAITING_TIME : Long = 1000

        val SHARED_PREF_SEUPDATA                        = "setupdData"

        val PREF_HAPTIC_CALL_NUMBER : String                        = "prefHapticCallNumber"
        val PREF_GYRO_CALL_NUMBER : String                          = "prefGyroCallNumber"
        val PREF_EMERGENCY_CALL_NUMBER : String                          = "prefEmergencyCallNumber"

        val MEASURE_WAITING_TIME : Long = 20000
        val MEASURE_MEASURING_TIME : Long = 30000
        var bIsStartMeasure : Boolean = false

        var measureingHeartBeat = ArrayList<Int>()
        var nAvgHeartBeat = 0

        val USER_STATUS_NORMAL = 0

        val DB_NAME_HEARTBEAT = "heartbeat.realm"
        val DB_NAME_TEMPERATURE = "temperature.realm"

        var STASTIC_CURRENT_STATE = "DAY"

        var alMeasuredData: MeasuredData? = null
        var alTemperatureData: TemperatureData? = null

        val REQUEST_ENABLE_BT = 1
        // Stops scanning after 10 seconds.
        val SCAN_PERIOD: Long = 10000
        val PERMISSION_REQUEST_COARSE_LOCATION = 1

        val HB_MEASUREMENT_DATA                       = "HB_Data"

        var nCurFunctionIndex                           = 1
        val SELECT_FUNCTION_INDEX                       = "Select_Index"

        val MAIN_FUNCTION_INDEX_HB                      = 1
        val MAIN_FUNCTION_INDEX_PRESSURE                = 2
        val MAIN_FUNCTION_INDEX_GYRO                    = 5
        val MAIN_FUNCTION_INDEX_TEMPERATURE             = 6
        val MAIN_FUNCTION_INDEX_STATISTICS              = 7
        val MAIN_FUNCTION_INDEX_SETTING                 = 8
        val MAIN_FUNCTION_INDEX_HB_RESULT               = 9
        val MAIN_FUNCTION_INDEX_REAR                    = 10

        val RECIEVE_DATA_PREFIX_TEMPERATURE                     = "TE"


        var curYearOfDay = 2018
        var curMonthOfDay = 11
        var curDayOfDay = 16
        var curYearOfMonth = 2018
        var curMonthOfMonth = 11


        val MESSAGE_SEND_HB                             = "HBMessage"
        val MESSAGE_SEND_FINEDUST                       = "FineDustMessage"
        val MESSAGE_SEND_GAS                            = "GasMessage"
        val MESSAGE_SEND_PRESSURE                       = "PressureMessage"
        val MESSAGE_SEND_REAR                           = "RearMessage"
        val MESSAGE_SEND_UV                             = "UVMessage"
        val MESSAGE_SEND_GYRO                           = "GyroMessage"
        val MESSAGE_SEND_TEMPERATURE                    = "TemperatureMessage"

        val ACTION_GYRO_SIGNAL                          = "1"
        val ACTION_REAR_SIGNAL                          = "1"

        // Address
        val MODULE_ADDRESS_HB                             = "98:D3:91:FD:5E:40"
        val MODULE_ADDRESS_PRESSURE                       = "98:D3:91:FD:5E:40"
        val MODULE_ADDRESS_GYRO                           = "4D:B1:2D:AB:1F:31"
        val MODULE_ADDRESS_REAR                           = "9E:C7:89:92:46:5F"
        val MODULE_ADDRESS_TEMPERATURE                    = "98:D3:91:FD:5E:40"
//        val MODULE_ADDRESS_TEMPERATURE                    = "20:19:09:06:45:34"
        val MODULE_ADDRESS_HC06                             = "98:D3:91:FD:5E:40"

        // Service UUID
        val MODULE_SERVICE_UUID_HB                              = "6E400001-B5A3-F393-E0A9-E50E24DCCA9F"
        val MODULE_SERVICE_UUID_PRESSURE                        = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
        val MODULE_SERVICE_UUID_GYRO                            = "19b10001-e8f2-537e-4f6c-d104768a1214"
        val MODULE_SERVICE_UUID_REAR                            = "19b10001-e8f2-537e-4f6c-d104768a1214"
        val MODULE_SERVICE_UUID_TEMPERATURE                     = "6E400001-B5A3-F393-E0A9-E50E24DCCA9D"


        // Characteristic UUID
        val MODULE_CHARACTERISTIC_UUID_HB                       = "6E400002-B5A3-F393-E0A9-E50E24DCCA9F"
        val MODULE_CHARACTERISTIC_UUID_PRESSURE                 = "6E400003-B5A3-F393-E0A9-E50E24DCCA9F"
        val MODULE_CHARACTERISTIC_UUID_GYRO                     = "00002902-0000-1000-8000-00805f9b34fb"
        val MODULE_CHARACTERISTIC_UUID_REAR                     = "00002902-0000-1000-8000-00805f9b34fb"
        val MODULE_CHARACTERISTIC_UUID_TEMPERATURE              = "6E400003-B5A3-F393-E0A9-E50E24DCCA9D"

        // values have to be globally unique
        val INTENT_ACTION_DISCONNECT: String = BuildConfig.APPLICATION_ID.toString() + ".Disconnect"
        val NOTIFICATION_CHANNEL: String = BuildConfig.APPLICATION_ID.toString() + ".Channel"
        val INTENT_CLASS_MAIN_ACTIVITY: String =
            BuildConfig.APPLICATION_ID.toString() + ".MainActivity"

        // values have to be unique within each app
        const val NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001
    }
}
