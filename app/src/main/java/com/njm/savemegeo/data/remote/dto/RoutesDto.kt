package com.njm.savemegeo.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.njm.savemegeo.domain.model.Routes

data class RoutesDto(
    @SerializedName("summary")
    val summary: String,
    @SerializedName("overview_polyline")
    val overview_polyline: OverviewPolylineDto,
    @SerializedName("legs")
    val legs: List<LegsDto>
)
{
    fun toRoutes(): Routes {
        return Routes(
            summary = summary,
            overview_polyline = overview_polyline.toOverviewPolyline(),
            legs = legs.map { it.toLegs() }
        )
    }
}
