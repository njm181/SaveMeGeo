package com.njm.savemegeo.domain.useCase

import com.njm.savemegeo.data.local.PreferencesDataStore

class SetVehicleLocationUseCase(private val preferencesDataStore: PreferencesDataStore) {

    suspend operator fun invoke(lat: Double, long: Double) {
        preferencesDataStore.setLatitude(latitude = lat.toString())
        preferencesDataStore.setLongitude(longitude = long.toString())
    }
}