package com.njm.savemegeo.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.njm.savemegeo.domain.model.Duration

data class DurationDto(
    @SerializedName("text")
    val text: String,
    @SerializedName("value")
    val value: Int
) {
    fun toDuration(): Duration {
        return Duration(
            text = text,
            value = value
        )
    }
}
