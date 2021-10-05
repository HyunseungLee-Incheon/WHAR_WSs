package com.crc.har.database

class DBModule {

    fun provideHeartBeatManager(): DBHeartBeatManager {
        return DBHeartBeatManager()
    }
}