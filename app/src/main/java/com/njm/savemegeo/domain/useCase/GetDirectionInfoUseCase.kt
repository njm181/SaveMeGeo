package com.njm.savemegeo.domain.useCase

import com.njm.savemegeo.domain.repository.GooglePlacesInfoRepository
import com.njm.savemegeo.util.Resource
import com.njm.savemegeo.domain.model.GooglePlacesInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDirectionInfoUseCase @Inject constructor (private val repository: GooglePlacesInfoRepository) {
    operator fun invoke(
        origin: String,
        destination: String,
        key: String
    ): Flow<Resource<GooglePlacesInfo>> = repository.getDirection(origin, destination, key)
}