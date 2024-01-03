package com.njm.savemegeo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore : DataStore<Preferences> by preferencesDataStore("LOCATION")
class PreferencesDataStore(context: Context) {
    val pref = context.dataStore

    companion object {
        var LATITUDE_KEY = stringPreferencesKey("LATITUDE_KEY")
        var LONGITUDE_KEY = stringPreferencesKey("LONGITUDE_KEY")
    }

    suspend fun setLatitude(latitude: String){
        pref.edit {
            it[LATITUDE_KEY] = latitude
        }
    }

    suspend fun setLongitude(longitude: String){
        pref.edit {
            it[LONGITUDE_KEY] = longitude
        }
    }

    fun getLatitude() = pref.data.map {
        it[LATITUDE_KEY]
    }

    fun getLongitude() = pref.data.map {
        it[LONGITUDE_KEY]
    }
}