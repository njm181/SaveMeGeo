package com.njm.savemegeo.data.repositoryImp

import com.njm.savemegeo.domain.model.GooglePlacesInfo
import com.njm.savemegeo.domain.repository.GooglePlacesInfoRepository
import com.njm.savemegeo.util.Resource
import com.njm.savemegeo.data.remote.GooglePlacesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GooglePlacesInfoRepositoryImplementation(): GooglePlacesInfoRepository {

    private fun provideGooglePlacesApi(): GooglePlacesApi {
        return Retrofit.Builder()
            .baseUrl(GooglePlacesApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GooglePlacesApi::class.java)
    }


    override fun getDirection(
        origin: String,
        destination: String,
        key: String
    ): Flow<Resource<GooglePlacesInfo>> = flow {
        emit(Resource.Loading())
        try {
            val directionData = provideGooglePlacesApi().getDirections(origin = origin, destination = destination, mode = "walking" ,key = key)
            emit(Resource.Success(data = directionData))
        } catch (e: HttpException){
            emit(Resource.Error(message = "Something is not right: $e"))
        }catch (e: IOException){
            emit(Resource.Error(message = "No internet connection: $e"))
        }
    }
}