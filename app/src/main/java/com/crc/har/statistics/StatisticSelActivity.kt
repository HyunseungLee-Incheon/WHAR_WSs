package com.crc.har.statistics

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.crc.har.R
import com.crc.har.main.MainActivity

class StatisticSelActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_statistic_sel)

        var tvToolbarTitle : TextView = findViewById(R.id.tv_toolbar_title)
        tvToolbarTitle.text = getString(R.string.str_statistic_title)

        var btToolbarBack : Button = findViewById(R.id.bt_toolbar_back)
        btToolbarBack.setOnClickListener(this)

        var btStatisticHB : Button = findViewById(R.id.bt_statistic_hb)
        btStatisticHB.setOnClickListener(this)

        var btStatisticTemp : Button = findViewById(R.id.bt_statistic_temp)
        btStatisticTemp.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bt_statistic_hb -> {
                val intent = Intent(this, StatisticActivity::class.java)
                startActivity(intent)
            }
            R.id.bt_statistic_temp -> {
                val intent = Intent(this, StatisticTemperatureActivity::class.java)
                startActivity(intent)
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
}
