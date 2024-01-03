package com.njm.savemegeo.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.njm.savemegeo.domain.model.GeocodedWaypoints

data class GeocodedWayPointsDto(
    @SerializedName("geocoder_status")
    val geocoder_status: String,
    @SerializedName("place_id")
    val place_id: String,
    @SerializedName("types")
    val types: List<String>
){
    fun toGeocodedWaypoints(): GeocodedWaypoints {
        return GeocodedWaypoints(
            geocoder_status = geocoder_status,
            place_id = place_id,
            types = types
        )
    }
}
