package com.crc.har.measure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.crc.har.R
import com.crc.har.base.Constants
import com.crc.har.main.MainActivity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class HeartBeatMeasureActivity : AppCompatActivity(), View.OnClickListener {

    var strReceiveData : String = ""

    private var time = 0;
    private var timerTask: Timer? = null;
    private var timerMeasureTask: Timer? = null;

    private var tvLoading : TextView? = null;
    private var bIsFinish: Boolean = false
    private var arHBData : ArrayList<String> = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heartbeatmeasure)


        var tvToolbarTitle : TextView = findViewById(R.id.tv_toolbar_title)
        tvToolbarTitle.text = getString(R.string.str_measure_title)

        var btToolbarBack : Button = findViewById(R.id.bt_toolbar_back)
        btToolbarBack.setOnClickListener(this)

        tvLoading = findViewById(R.id.tvLoading)
        tvLoading?.text = "0 %"

        Constants.bIsStartMeasure = false

        Constants.nAvgHeartBeat = 0

        timerTask = timer(period = 3000) {
            startInitMeasure()
            timerTask?.cancel()
        }

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
            Constants.MESSAGE_SEND_HB)
        )
    }

    private fun startInitMeasure() {
        timerMeasureTask = timer(period = 1000) {
            time++

            // data measure
            if(!Constants.bIsStartMeasure && !bIsFinish && time >= 2) {
                Constants.bIsStartMeasure = true
            }
            if(Constants.bIsStartMeasure && !bIsFinish && time >= 5) {
                Constants.bIsStartMeasure = false
                bIsFinish = true
                timerMeasureTask?.cancel()
                finishMeasure()
            }
//            Log.e("eleutheria", "time : ${time}")
            runOnUiThread {
                tvLoading?.text = "${time * 10} ${getString(R.string.str_common_percentage)}"
            }
        }

    }

    private fun finishMeasure() {

        val avgHeartBeat = sumHeartBeat(arHBData).toInt()
        Constants.nAvgHeartBeat = avgHeartBeat

        val intent = Intent(this, HeartBeatResultActivity::class.java)
        intent.putExtra(Constants.HB_MEASUREMENT_DATA, avgHeartBeat)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

//        startActivity<LoadingResultActivity>()
//        Constants.nCurFunctionIndex = Constants.MAIN_FUNCTION_INDEX_HB_RESULT
//        startActivity(intentFor<LoadingActivity>(Constants.SELECT_FUNCTION_INDEX to Constants.MAIN_FUNCTION_INDEX_HB_RESULT).clearTask().newTask())

//        startActivity<HeartBeatResultActivity>(Constants.HB_MEASUREMENT_DATA to arHBData)
        Log.e("eleutheria", "finish measure")
    }

    private fun sumHeartBeat(measuredHeartBeat: ArrayList<String>): Long {

        var sumHeartBeat : Long = 0

        for(heartBeat in measuredHeartBeat) {
            sumHeartBeat += heartBeat.toInt()
        }

        var avgHeartBeat : Long = 0
        if(sumHeartBeat > 0) {
            avgHeartBeat = sumHeartBeat / measuredHeartBeat.size
        }

        return avgHeartBeat
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent!!.hasExtra("value")) {
                val message = intent.getStringExtra("value")
                Log.e("eleutheria", "message : $message")

                if(Constants.bIsStartMeasure && !bIsFinish) {
                    Log.e("eleutheria", "HB : $message")
                    if (message != null) {
                        if(message.toInt() > 0) {
                            arHBData.add(message)
                        }
                    }
                }

            }
        }
    }
}