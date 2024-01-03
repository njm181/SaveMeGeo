package com.njm.savemegeo.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.njm.savemegeo.domain.model.GooglePlacesInfo

data class GooglePlacesInfoDto(
    @SerializedName("geocoded_waypoints")
    val geocoded_waypoints: List<GeocodedWayPointsDto>,
    @SerializedName("routes")
    val routes: List<RoutesDto>,
    @SerializedName("status")
    val status: String
){
    fun toGooglePlacesInfo(): GooglePlacesInfo {
        return GooglePlacesInfo(
            geocoded_waypoints = geocoded_waypoints.map { it.toGeocodedWaypoints() },
            routes = routes.map { it.toRoutes() },
            status = status
        )
    }
}
