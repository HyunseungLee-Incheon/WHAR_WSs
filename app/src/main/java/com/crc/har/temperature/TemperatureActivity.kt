package com.crc.har.temperature

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.crc.har.R
import com.crc.har.base.CommonUtils
import com.crc.har.base.Constants
import com.crc.har.base.CurrentDate
import com.crc.har.base.TemperatureData
import com.crc.har.database.DBTemperatureModel
import com.crc.har.main.MainActivity
import io.realm.Realm

class TemperatureActivity : AppCompatActivity(), View.OnClickListener {

    val realm: Realm = Realm.getDefaultInstance()
    val commonUtils = CommonUtils()

    lateinit var tvTemperatureText: TextView
    lateinit var ivTemperatureBg: ImageView
    var nTemperature = 36
    var strReceiveData = ""

//    private val mBtHandler = BluetoothHandler()
//    private val mBluetoothClassicManager: BluetoothClassicManager = BluetoothClassicManager.getInstance()
//    private var mIsConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_temperature)

        var tvToolbarTitle : TextView = findViewById(R.id.tv_toolbar_title)
        tvToolbarTitle.text = getString(R.string.str_temperature_title)

        var btToolbarBack : Button = findViewById(R.id.bt_toolbar_back)
        btToolbarBack.setOnClickListener(this)

        tvTemperatureText = findViewById(R.id.tv_temperature_text)
        ivTemperatureBg = findViewById(R.id.iv_temperature_bg)

        displayTemperature(nTemperature)
    }

    private fun displayTemperature(nTemperature: Int) {
        var strCurTemperature = nTemperature.toString() +  getString(R.string.str_main_default_temperature)
        tvTemperatureText.text = strCurTemperature
    }

    private fun saveTemperature(nTemperature: Int) {
        val curDate = commonUtils.getCurrentDate()
        val mResultDate : CurrentDate = CurrentDate()
        mResultDate.nYear = curDate[0].toInt()
        mResultDate.nMonth = curDate[1].toInt()
        mResultDate.nDay = curDate[2].toInt()
        mResultDate.nHour = curDate[3].toInt()
        mResultDate.nMinute = curDate[4].toInt()
        mResultDate.nSecond = curDate[5].toInt()

        realm.beginTransaction()

        var newId: Long = 1
        if(realm.where(DBTemperatureModel::class.java).max("id") != null) {
            newId = realm.where(DBTemperatureModel::class.java).max("id") as Long + 1
        }
        val storedData: TemperatureData = commonUtils.storeTemperatureData(mResultDate, nTemperature)
        val insertData = realm.createObject(DBTemperatureModel::class.java, newId)
        insertData.year = storedData.nYear
        insertData.month = storedData.nMonth
        insertData.day = storedData.nDay
        insertData.hour = storedData.nHour
        insertData.minute = storedData.nMinute
        insertData.second = storedData.nSecond
        insertData.temperature = storedData.nTemperature

        realm.commitTransaction()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bt_toolbar_back -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter(
            Constants.MESSAGE_SEND_TEMPERATURE))
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent!!.hasExtra("value")) {
                val message = intent.getStringExtra("value")

                if (message != null) {
                    saveTemperature(message.toInt())
                }
                if (message != null) {
                    displayTemperature(message.toInt())
                }

            }
        }

    }
}