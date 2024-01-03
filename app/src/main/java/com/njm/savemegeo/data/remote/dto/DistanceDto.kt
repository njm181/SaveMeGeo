package com.njm.savemegeo.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.njm.savemegeo.domain.model.Distance

data class DistanceDto(
    @SerializedName("text")
    val text: String,
    @SerializedName("value")
    val value: Int
) {
    fun toDistance(): Distance {
        return Distance(
            text = text,
            value = value
        )
    }
}
