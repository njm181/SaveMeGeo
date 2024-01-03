package com.njm.savemegeo.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.njm.savemegeo.domain.model.OverviewPolyline

data class OverviewPolylineDto(
    @SerializedName("points")
    val points: String
){
    fun toOverviewPolyline(): OverviewPolyline {
        return OverviewPolyline(
            points = points
        )
    }
}

