package com.crc.har.database

class DBTemperatureModule {
    fun provideTemperatureManager(): DBTemperatureManager {
        return DBTemperatureManager()
    }
}