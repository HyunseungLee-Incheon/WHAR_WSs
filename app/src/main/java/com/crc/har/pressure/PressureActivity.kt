package com.crc.har.pressure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.crc.har.R
import com.crc.har.base.Constants
import com.crc.har.main.MainActivity
import java.util.*

class PressureActivity : AppCompatActivity(), View.OnClickListener {

    val handler: Handler = Handler1()
    private val mAlertTimer by lazy { Timer() }
    var isAlertOn = false
    var isEmergency = false

    lateinit var iv_alert: ImageView
    var strReceiveData : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pressure)

        var tvToolbarTitle : TextView = findViewById(R.id.tv_toolbar_title)
        tvToolbarTitle.text = getString(R.string.str_pressure_title)

        var btToolbarBack : Button = findViewById(R.id.bt_toolbar_back)
        btToolbarBack.setOnClickListener(this)


        iv_alert = findViewById<ImageView>(R.id.iv_alert)
        iv_alert.setOnClickListener(this)
        iv_alert.setImageResource(R.drawable.haptic_pressure_siren_image1)
    }

    private fun aletByPressure() {
        val task = object : TimerTask() {
            override fun run() {

                isEmergency = true
//                Log.e("eleutheria", "aletByPressure")
                val msg = handler.obtainMessage()
                handler.sendMessage(msg)

            }

        }
        mAlertTimer.schedule(task, 1000,1000)

    }

    inner class Handler1 : Handler() {
        override fun handleMessage(msg: Message) = if(isAlertOn) {
            iv_alert.setImageResource(R.drawable.haptic_pressure_siren_image1)

//            Log.e("eleutheria", "isAlertOn : On")
            isAlertOn = false
        } else {
            iv_alert.setImageResource(R.drawable.haptic_pressure_siren_image2)

//            Log.e("eleutheria", "isAlertOn : Off")
            isAlertOn = true
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, IntentFilter(
            Constants.MESSAGE_SEND_PRESSURE)
        )
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bt_toolbar_back -> {
                onBackPressed()
            }
            R.id.iv_alert -> {
                aletByPressure()
//                sendSMS()
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }


    private fun sendSMS() {
        val strPhoneNumber = Constants.strEmergencyNumber

        val smsManager = SmsManager.getDefault()

//        val message = "Emergency Message!! "
        val message = "Emergency!! I need rescue!! track my location!!"
        val strFirstString = strPhoneNumber.substring(0, 1)

        if(strFirstString.equals("0")) {
            Log.e("eleutheria", "strPhoneNumber : $strPhoneNumber")
            smsManager.sendTextMessage(strPhoneNumber, null, message, null, null)
        }
    }

    private fun sendCall() {
        var strPhoneNumber = Constants.strEmergencyNumber

        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$strPhoneNumber"))
        startActivity(intent)
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent!!.hasExtra("value")) {
                val message = intent!!.getStringExtra("value")
//                Log.e("eleutheria", "message : $message")

                if (message != null) {
                    if (message.contains("\n")) {
//                        HB:83!/BT:23.68!/
                        strReceiveData += message
                        if(strReceiveData.contains("P")) {
                            Log.e("eleutheria", "pressure alert")

                            if(!isEmergency) {
                                aletByPressure()
//                                Log.e("eleutheria", "strPhoneNumber : ${Constants.strHapticNumber}")
                                sendSMS()
                                sendCall()
                            }
                        }
                        strReceiveData = ""
                    } else {
                        strReceiveData += message
                    }
                }


//                if(message == ".") {
////                    Log.e("eleutheria", "strReceiveData : $strReceiveData")
//                    var arData = strReceiveData.split("\r\n")
//
//                    for(data in arData) {
//                        if(data.contains("P")) {
//                            Log.e("eleutheria", "data : $data")
//
//                            if(data == "P1" || data == ".P1") {
//                                Log.e("eleutheria", "pressure alert")
//                                aletByPressure()
////                                Log.e("eleutheria", "strPhoneNumber : ${Constants.strHapticNumber}")
////                                sendSMS()
////                                sendCall()
//                            }
//                        }
//                    }
//                    strReceiveData = ""
//                } else {
//                    strReceiveData += message
//                }


            }
        }
    }

}
