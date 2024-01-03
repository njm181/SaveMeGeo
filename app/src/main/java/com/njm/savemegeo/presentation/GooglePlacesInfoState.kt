package com.njm.savemegeo.presentation

import com.njm.savemegeo.domain.model.GooglePlacesInfo

data class GooglePlacesInfoState(
    val direction: GooglePlacesInfo? = null,
    val isLoading: Boolean = false
)
