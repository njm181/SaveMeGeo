package com.njm.savemegeo.domain.repository

import com.njm.savemegeo.util.Resource
import com.njm.savemegeo.domain.model.GooglePlacesInfo
import kotlinx.coroutines.flow.Flow

interface GooglePlacesInfoRepository {
    fun getDirection(origin: String, destination: String, key: String): Flow<Resource<GooglePlacesInfo>>
}