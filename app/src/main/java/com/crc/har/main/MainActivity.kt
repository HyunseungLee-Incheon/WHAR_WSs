package com.crc.har.main

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.crc.har.base.CommonUtils
import com.crc.har.base.Constants
import com.crc.har.R
import com.crc.har.gyro.GyroActivity
import com.crc.har.pressure.PressureActivity
import com.crc.har.setting.SettingActivity
import com.crc.har.statistics.StatisticSelActivity
import com.crc.har.temperature.TemperatureActivity
import io.realm.Realm

class MainActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemClickListener {
    val realm: Realm = Realm.getDefaultInstance()

    lateinit var context: Context
    lateinit var mActivity: Activity

    internal var PERMISSIONS = arrayOf(
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.BLUETOOTH",
        "android.permission.BLUETOOTH_ADMIN",
        "android.permission.SEND_SMS",
        "android.permission.READ_PHONE_STATE",
        "android.permission.ACCESS_NOTIFICATION_POLICY",
        "android.permission.READ_CONTACTS",
        "android.permission.CALL_PHONE",
        "android.permission.ACCESS_COARSE_LOCATION"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var tvToolbarTitle : TextView = findViewById(R.id.tv_toolbar_title)
        tvToolbarTitle.text = getString(R.string.str_main_title)
        tvToolbarTitle.setOnClickListener(this)

        context = applicationContext
        mActivity = this

        val mainIconList = arrayListOf<Int>(
            R.drawable.main_icon_heartbeat,
            R.drawable.main_icon_haptic_pressure,
            R.drawable.main_icon_gyro,
            R.drawable.main_icon_temperature,
            R.drawable.main_icon_statistics,
            R.drawable.main_icon_set_up
        )

        val listAdapter = MainAdapter(this, mainIconList)

        var gvMainList : GridView = findViewById(R.id.gvMainList)
        gvMainList.adapter = listAdapter
        gvMainList.setOnItemClickListener(this)

        var preferences: SharedPreferences = getSharedPreferences(Constants.SHARED_PREF_SEUPDATA, Context.MODE_PRIVATE)

        Constants.strEmergencyNumber =
            preferences.getString(Constants.PREF_EMERGENCY_CALL_NUMBER, Constants.strEmergencyNumber).toString()

        var commonUtils = CommonUtils()
        var curDate = commonUtils.getCurrentDate()

        Constants.curYearOfDay = curDate[0].toInt()
        Constants.curMonthOfDay = curDate[1].toInt()
        Constants.curDayOfDay = curDate[2].toInt()
        Constants.curYearOfMonth = curDate[0].toInt()
        Constants.curMonthOfMonth = curDate[1].toInt()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {
                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE)
            }
//            if (!notificationManager.isNotificationPolicyAccessGranted) {
//                mActivity.runOnUiThread {
//                    Toast.makeText(
//                        context,
//                        context.getString(R.string.str_toast_turn_off_do_not_disturb),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
//                startActivity(intent)
//            }
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        var result: Int
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (perms in permissions) {
            result = ContextCompat.checkSelfPermission(this, perms)
            if (result == PackageManager.PERMISSION_DENIED) {
                //허가 안된 퍼미션 발견
                return false
            }
        }
        //모든 퍼미션이 허가되었음
        return true
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.e("eleutheria", "position : " + position)
        when(position) {
            0 -> { // heartbeat
                Constants.nCurFunctionIndex = Constants.MAIN_FUNCTION_INDEX_HB
                val intent = Intent(this, LoadingClassicActivity::class.java)
                intent.putExtra(Constants.SELECT_FUNCTION_INDEX, Constants.MAIN_FUNCTION_INDEX_HB)
                startActivity(intent)

//                val intent = Intent(this, HeartBeatMeasureActivity::class.java)
//                startActivity(intent)
            }
            1 -> { // pressure
                Constants.nCurFunctionIndex = Constants.MAIN_FUNCTION_INDEX_PRESSURE
                val intent = Intent(this, LoadingClassicActivity::class.java)
                intent.putExtra(Constants.SELECT_FUNCTION_INDEX, Constants.MAIN_FUNCTION_INDEX_PRESSURE)
                startActivity(intent)

//                val intent = Intent(this, PressureActivity::class.java)
//                startActivity(intent)
            }
            2 -> { // gyro
                Constants.nCurFunctionIndex = Constants.MAIN_FUNCTION_INDEX_GYRO
                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtra(Constants.SELECT_FUNCTION_INDEX, Constants.MAIN_FUNCTION_INDEX_GYRO)
                startActivity(intent)

//                val intent = Intent(this, GyroActivity::class.java)
//                startActivity(intent)
            }
            3 -> { // temperature
                Constants.nCurFunctionIndex = Constants.MAIN_FUNCTION_INDEX_TEMPERATURE
                val intent = Intent(this, LoadingClassicActivity::class.java)
                intent.putExtra(Constants.SELECT_FUNCTION_INDEX, Constants.MAIN_FUNCTION_INDEX_TEMPERATURE)
                startActivity(intent)

//                val intent = Intent(this, TemperatureActivity::class.java)
//                startActivity(intent)
            }
            4 -> { // statistic
                val intent = Intent(this, StatisticSelActivity::class.java)
                startActivity(intent)
            }
            5 -> { // setting
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }
        }
    }
    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.tv_toolbar_title -> {
//                makeTestData()
                val intent = Intent(this, LoadingActivity::class.java)
                intent.putExtra(Constants.SELECT_FUNCTION_INDEX, Constants.MAIN_FUNCTION_INDEX_TEMPERATURE)
                startActivity(intent)
            }
        }

    }

    companion object {

        //여기서부턴 퍼미션 관련 메소드
        private val PERMISSION_REQUEST_COARSE_LOCATION = 456
        internal val PERMISSIONS_REQUEST_CODE = 1000
    }
}