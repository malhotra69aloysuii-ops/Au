package com.example.di

import android.content.Context
import com.example.data.local.EpicDatabase
import com.example.data.local.GameDao

class DatabaseModule(context: Context) {

    val database: EpicDatabase by lazy {
        EpicDatabase.getInstance(context)
    }

    val gameDao: GameDao by lazy {
        database.gameDao()
    }
}
