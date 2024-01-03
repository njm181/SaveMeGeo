package com.njm.savemegeo.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.njm.savemegeo.domain.model.Legs

data class LegsDto(
    @SerializedName("distance")
    val distance: DistanceDto,
    @SerializedName("duration")
    val duration: DurationDto
){
    fun toLegs(): Legs {
        return Legs(
            distance = distance.toDistance(),
            duration = duration.toDuration()
        )
    }
}
