package com.crc.har.statistics

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.crc.har.R
import com.crc.har.base.Constants
import com.crc.har.main.MainActivity

class StatisticActivity : AppCompatActivity(), View.OnClickListener {

    private var frgStatisticDay = StasticDayFragment()
    private var frgStatisticMonth = StatisticMonthFragment()


    private lateinit var btStatisticDay: Button
    private lateinit var btStatisticMonth: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        val tvToolbarTitle : TextView = findViewById(R.id.tv_toolbar_title)
        tvToolbarTitle.text = getString(R.string.str_statistic_HeartBeat_title)

        val btToolbarBack : Button = findViewById(R.id.bt_toolbar_back)
        btToolbarBack.setOnClickListener(this)

        btStatisticDay = findViewById(R.id.bt_statistic_day)
        btStatisticDay.setOnClickListener(this)

        btStatisticMonth = findViewById(R.id.bt_statistic_month)
        btStatisticMonth.setOnClickListener(this)

        setCurrentButtonState()

        changeFragment(frgStatisticDay)
    }

    override fun onClick(v: View?) {

        when(v?.id) {
            R.id.bt_statistic_day -> {
                changeFragment(frgStatisticDay)
            }
            R.id.bt_statistic_month -> {
                changeFragment(frgStatisticMonth)
            }
            R.id.bt_toolbar_back -> {
                onBackPressed()
            }
        }

    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun changeFragment(frag : Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        transaction.replace(R.id.flFragContainer, frag)
        transaction.addToBackStack(null)
        transaction.commit()

        if(frag == frgStatisticDay) {
            Constants.STASTIC_CURRENT_STATE = "DAY"
        } else {
            Constants.STASTIC_CURRENT_STATE = "MONTH"
        }
        setCurrentButtonState()
    }

    private fun setCurrentButtonState() {
        if(Constants.STASTIC_CURRENT_STATE == "DAY") {
            btStatisticDay.setBackgroundResource(R.drawable.statistics_daily_daily_button_normal)
            btStatisticMonth.setBackgroundResource(R.drawable.statistics_daily_monthly_button_normal)
        } else {
            btStatisticDay.setBackgroundResource(R.drawable.statistics_monthly_daily_button_normal)
            btStatisticMonth.setBackgroundResource(R.drawable.statistics_monthly_monthly_button_normal)
        }
    }
}
