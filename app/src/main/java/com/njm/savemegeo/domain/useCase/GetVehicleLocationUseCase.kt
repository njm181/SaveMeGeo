package com.njm.savemegeo.domain.useCase

import com.google.android.gms.maps.model.LatLng
import com.njm.savemegeo.data.local.PreferencesDataStore
import com.njm.savemegeo.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException

class GetVehicleLocationUseCase(private val preferencesDataStore: PreferencesDataStore) {

    suspend operator fun invoke(): Flow<Resource<LatLng>>  = flow {
        emit(Resource.Loading())
        try {
            preferencesDataStore.getLatitude().collect { lat ->
                if (lat != null) {
                    preferencesDataStore.getLongitude().collect{ long ->
                        if (long != null) {
                            emit(Resource.Success(LatLng(lat.toDouble(), long.toDouble())))
                        }
                    }
                }
            }
        } catch (e: IOException) {
            emit(Resource.Error(message = "Error getting vehicle's location"))
        }

    }
}