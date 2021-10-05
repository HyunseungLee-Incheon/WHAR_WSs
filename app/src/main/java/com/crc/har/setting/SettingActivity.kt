package com.crc.har.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.crc.har.R
import com.crc.har.base.Constants

class SettingActivity : AppCompatActivity(), View.OnClickListener {

    private var settings: SharedPreferences? = null
    lateinit var tvEmergencyNumber: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_setting)

        val tvToolbarTitle : TextView = findViewById(R.id.tv_toolbar_title)
        tvToolbarTitle.text = getString(R.string.str_setting_title)

        val btToolbarBack : Button = findViewById(R.id.bt_toolbar_back)
        btToolbarBack.setOnClickListener(this)

        settings = getSharedPreferences(Constants.SHARED_PREF_SEUPDATA, Context.MODE_PRIVATE)

        tvEmergencyNumber = findViewById(R.id.tv_emergency_number)

        tvEmergencyNumber.text = Constants.strEmergencyNumber

        val btEmergency119 : Button = findViewById(R.id.bt_emergency_119)
        btEmergency119.setOnClickListener(this)
        val btEmergencyContact : Button = findViewById(R.id.bt_emergency_contact)
        btEmergencyContact.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bt_emergency_119 -> {
                Constants.strEmergencyNumber = getString(R.string.str_setting_119)
                tvEmergencyNumber.text = Constants.strEmergencyNumber

                val editor = settings!!.edit()
                editor.putString(Constants.PREF_EMERGENCY_CALL_NUMBER, Constants.strEmergencyNumber)
                editor.apply()
            }
            R.id.bt_emergency_contact -> {
                val contactIntent = Intent(Intent.ACTION_PICK)
                contactIntent.data = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                startActivityForResult(contactIntent, 1)
//                LoadingActivity.mBluetoothLeService!!.writeTimeCharacteristic(et_heartbeat.text.toString())
            }
            R.id.bt_toolbar_back -> {
                onBackPressed()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {

            val cursor = contentResolver.query(
                data!!.getData()!!,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ), null, null, null
            )
            cursor!!.moveToFirst()
            val name = cursor.getString(0)        //이름 얻어오기
            val number = cursor.getString(1)

            if(requestCode == 1) {
                Constants.strEmergencyNumber = number
                tvEmergencyNumber.text = Constants.strEmergencyNumber

                val editor = settings!!.edit()
                editor.putString(Constants.PREF_EMERGENCY_CALL_NUMBER, Constants.strEmergencyNumber)
                editor.apply()
            }
        }
    }
}