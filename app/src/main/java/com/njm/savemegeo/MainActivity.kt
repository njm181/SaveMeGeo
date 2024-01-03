package com.njm.savemegeo

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.njm.savemegeo.ui.theme.SaveMeGeoTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.njm.savemegeo.presentation.GooglePlacesInfoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val googlePlacesViewModel: GooglePlacesInfoViewModel = viewModel()
            var isMapLoaded by remember { mutableStateOf(false) }
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(true){
                googlePlacesViewModel.evenFlow.collectLatest { event ->
                    when(event){
                        is GooglePlacesInfoViewModel.UIEvent.ShowSnackBar ->{
                            snackbarHostState.showSnackbar(
                                message = event.message
                            )
                        }
                    }
                }
            }

            SaveMeGeoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState){
                                Snackbar(snackbarData = it, containerColor = Color.Gray, contentColor = Color.White)
                            }
                        },
                    ) { contentPadding ->
                        GetLocationScreen(isMapLoaded = isMapLoaded, isMapLoadedOnChange = { value -> isMapLoaded = value }, googlePlacesViewModel = googlePlacesViewModel)
                    }

                }
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun GoogleMapView(
    modifier: Modifier,
    onMapLoaded: () -> Unit,
    googlePlacesInfoViewModel: GooglePlacesInfoViewModel,
) {
    val currentLocationState by googlePlacesInfoViewModel.currentLocation.collectAsState()
    val vehicleLocationState by googlePlacesInfoViewModel.vehicleLocation.collectAsState()
    LaunchedEffect(true){
        googlePlacesInfoViewModel.getDirection(key = MapKey.KEY)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(vehicleLocationState, 16f)
    }

    var mapProperties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
    }
    var uiSettings by remember {
        mutableStateOf(
            MapUiSettings(compassEnabled = false)
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = uiSettings,
        onMapLoaded = onMapLoaded,
        googleMapOptionsFactory = {
            GoogleMapOptions().camera(
                CameraPosition.fromLatLngZoom(
                    vehicleLocationState,
                    16f
                )
            )
        },
    ){
        Marker(
            state = rememberMarkerState(position = vehicleLocationState),
            title = "You",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        )

        Marker(
            state = rememberMarkerState(position = currentLocationState),
            title = "Your Vehicle",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )


        Polyline(points = googlePlacesInfoViewModel.polyLinesPoints.value, onClick = {
            Log.d(TAG, "${it.points} was clicked")
        })

    }
}


@Composable
fun GetLocationScreen(
    isMapLoadedOnChange: (Boolean) -> Unit,
    googlePlacesViewModel: GooglePlacesInfoViewModel,
    isMapLoaded: Boolean
) {
    val showMap by googlePlacesViewModel.showMap.collectAsState()
    val context = LocalContext.current
    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted: Boolean ->
                if (isGranted) {
                    Toast.makeText(context, "You can save your current location now", Toast.LENGTH_SHORT).show()
                }
            })

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp), horizontalArrangement = Arrangement.Center) {
            Button(onClick = {
                googlePlacesViewModel.getVehicleLocation()
            }) {
                Text(text = "Go to car's location")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (googlePlacesViewModel.hasLocationPermission()) {
                        // Permission already granted, update the location
                        googlePlacesViewModel.getCurrentLocation(saveLocation = true)
                    } else {
                        // Request location permission
                        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            ) {
                Text(text = "Save vehicle's location")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (showMap){
            AnimatedVisibility(
                modifier = Modifier
                    .fillMaxSize(),
                visible = showMap,
                enter = fadeIn(),
                exit = fadeOut()
            ){
                GoogleMapView(
                    modifier = Modifier.fillMaxSize(),
                    onMapLoaded = {
                        isMapLoadedOnChange(true)
                    },
                    googlePlacesInfoViewModel = googlePlacesViewModel,
                )
            }

        }
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SaveMeGeoTheme {
        GetLocationScreen(
            isMapLoaded = true,
            isMapLoadedOnChange = {true},
            googlePlacesViewModel = GooglePlacesInfoViewModel(Application())
        )
    }
}