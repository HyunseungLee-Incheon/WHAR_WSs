package com.crc.har.database

import com.crc.har.base.Constants
import com.crc.har.base.MeasuredData
import io.realm.Realm
import io.realm.RealmConfiguration

class DBHeartBeatManager {

    val realm: Realm by lazy {
        val config = RealmConfiguration.Builder()
            .name(Constants.DB_NAME_HEARTBEAT)
            .schemaVersion(2)
            .build()
        Realm.getInstance(config)
    }

    fun find(id: Long): DBHeartBeatModel? {
        return realm.where(DBHeartBeatModel::class.java).equalTo("id", id).findFirst()
    }

    fun findAll(): List<DBHeartBeatModel> {
        return realm.where(DBHeartBeatModel::class.java).findAll()
    }

    fun insert(storedData: MeasuredData) {
        realm.beginTransaction()
        var newId: Long = 1
        if(realm.where(DBHeartBeatModel::class.java).max("id") != null) {
            newId = realm.where(DBHeartBeatModel::class.java).max("id") as Long + 1
        }

        val insertData = realm.createObject(DBHeartBeatModel::class.java, newId)
        insertData.year = storedData.nYear
        insertData.month = storedData.nMonth
        insertData.day = storedData.nDay
        insertData.hour = storedData.nHour
        insertData.minute = storedData.nMinute
        insertData.second = storedData.nSecond
        insertData.heartbeat = storedData.nHeartbeat
        insertData.status = storedData.nStatus

        realm.commitTransaction()

    }
}