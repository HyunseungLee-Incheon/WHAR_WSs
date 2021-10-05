package com.crc.har.database

import com.crc.har.base.Constants
import com.crc.har.base.TemperatureData
import io.realm.Realm
import io.realm.RealmConfiguration

class DBTemperatureManager {
    val realm: Realm by lazy {
        val config = RealmConfiguration.Builder()
            .name(Constants.DB_NAME_TEMPERATURE)
            .schemaVersion(2)
            .build()
        Realm.getInstance(config)
    }

    fun find(id: Long): DBTemperatureModel? {
        return realm.where(DBTemperatureModel::class.java).equalTo("id", id).findFirst()
    }

    fun findAll(): List<DBTemperatureModel> {
        return realm.where(DBTemperatureModel::class.java).findAll()
    }

    fun insert(storedData: TemperatureData) {
        realm.beginTransaction()
        var newId: Long = 1
        if(realm.where(DBTemperatureModel::class.java).max("id") != null) {
            newId = realm.where(DBTemperatureModel::class.java).max("id") as Long + 1
        }

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
}