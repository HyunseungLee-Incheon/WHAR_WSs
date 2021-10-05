package com.crc.har.database

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DBHeartBeatModel : RealmObject() {


    @PrimaryKey
    var id : Long = 0
    var year : Int = 2018
    var month : Int = 1
    var day : Int = 1
    var hour : Int = 1
    var minute : Int = 1
    var second : Int = 1
    var heartbeat : Int = 1
    var status : Int = 0
}