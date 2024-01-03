package com.njm.savemegeo.presentation

import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.njm.savemegeo.data.local.PreferencesDataStore
import com.njm.savemegeo.util.Resource
import com.njm.savemegeo.data.repositoryImp.GooglePlacesInfoRepositoryImplementation
import com.njm.savemegeo.domain.useCase.GetDirectionInfoUseCase
import com.njm.savemegeo.domain.useCase.GetVehicleLocationUseCase
import com.njm.savemegeo.domain.useCase.SetVehicleLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait

class GooglePlacesInfoViewModel(aplication: Application): AndroidViewModel(aplication) {

    private val context = getApplication<Application>().applicationContext

    private val getDirectionInfoUseCase: GetDirectionInfoUseCase =
        GetDirectionInfoUseCase(
            repository = GooglePlacesInfoRepositoryImplementation()
        )

    private val getVehicleLocationUseCase: GetVehicleLocationUseCase =
        GetVehicleLocationUseCase(preferencesDataStore = PreferencesDataStore(context = context))

    private val setVehicleLocationUseCase: SetVehicleLocationUseCase =
        SetVehicleLocationUseCase(preferencesDataStore = PreferencesDataStore(context = context))

    private val _googlePlacesInfoState = mutableStateOf(GooglePlacesInfoState())
    val googlePlacesInfoState: State<GooglePlacesInfoState> = _googlePlacesInfoState

    private val _polyLinesPoints = MutableStateFlow<List<LatLng>>(emptyList())
    val polyLinesPoints: StateFlow<List<LatLng>>
        get() = _polyLinesPoints

    private val _vehicleLocation = MutableStateFlow(LatLng(0.0,0.0))
    val vehicleLocation: StateFlow<LatLng>
        get() = _vehicleLocation

    private val _currentLocation = MutableStateFlow(LatLng(0.0,0.0))
    val currentLocation: StateFlow<LatLng>
        get() = _currentLocation

    private val _showMap = MutableStateFlow(false)
    val showMap: StateFlow<Boolean>
        get() = _showMap

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val evenFlow = _eventFlow.asSharedFlow()

    fun getDirection(key: String){
        viewModelScope.launch(Dispatchers.IO) {
            getDirectionInfoUseCase(
                origin = "${_vehicleLocation.value.latitude}, ${_vehicleLocation.value.longitude}",
                destination = "${_currentLocation.value.latitude}, ${_currentLocation.value.longitude}",
                key = key
            ).onEach { res ->
                when(res){
                    is Resource.Success ->{
                        _googlePlacesInfoState.value = googlePlacesInfoState.value.copy(
                            direction = res.data,
                            isLoading = false
                        )
                        googlePlacesInfoState.value.direction?.routes?.get(0)?.overview_polyline?.points?.let { decoPoints(points = it) }
                        Log.d(TAG, "POLYLINE:  ${googlePlacesInfoState.value.direction?.routes?.get(0)?.overview_polyline?.points}")
                        _eventFlow.emit(UIEvent.ShowSnackBar(message = "Route yo your vehicle"))
                    }
                    is Resource.Error -> {
                        _eventFlow.emit(UIEvent.ShowSnackBar(message = res.message?:"Unknown Error"))
                    }
                    is Resource.Loading -> {
                        _eventFlow.emit(UIEvent.ShowSnackBar(message = "Loading Direction"))
                    }

                    else -> {}
                }
            }.launchIn(this)
        }
    }


    fun getVehicleLocation(){
        viewModelScope.launch(Dispatchers.IO){
            getVehicleLocationUseCase().onEach { res ->
                when(res){
                    is Resource.Success ->{
                        res.data?.let {
                            _vehicleLocation.value = it
                            getCurrentLocation(saveLocation = false)
                        }
                    }
                    is Resource.Error -> {
                    }
                    is Resource.Loading -> {
                    }

                    else -> {}
                }
            }.launchIn(this)

        }
    }

    private fun setVehicleLocation(lat: Double, long: Double){
        viewModelScope.launch(Dispatchers.IO) {
            setVehicleLocationUseCase.invoke(lat = lat, long = long)
            withContext(Dispatchers.Main){
                Toast.makeText(context, "Vehicle's location saved!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getCurrentLocation(saveLocation: Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (
                ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return@launch
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val long = location.longitude
                        if (saveLocation){
                            setVehicleLocation(lat = lat, long = long)
                        } else {
                            _showMap.value = true
                            _currentLocation.value = LatLng(lat, long)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun decoPoints(points: String): List<LatLng>{
        _polyLinesPoints.value = decodePoly(points)
        return decodePoly(points);
    }

    /**
     * Method to decode polyline points
     */
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }

    sealed class UIEvent{
        data class ShowSnackBar(val message: String): UIEvent()
    }
}