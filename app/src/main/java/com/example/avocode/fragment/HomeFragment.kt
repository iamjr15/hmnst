package com.example.avocode.fragment


import android.Manifest
import android.app.Activity
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color.GREEN
import android.graphics.Color.RED
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.avocode.R
import com.example.avocode.activity.HomeActivity
import com.example.avocode.config.Constants.DEFAULT_LOCATION_UPDATE_TIME_INTERVAL
import com.example.avocode.config.Constants.DocumentFields.SHARED_MESSAGE
import com.example.avocode.config.Constants.FRAGMENT_HOME
import com.example.avocode.models.FamilyMemberData
import com.example.avocode.repo.UserImpl
import com.example.avocode.utils.Util
import com.example.avocode.utils.Util.Companion.displayLocationSettingsRequest
import com.example.avocode.utils.addBorder
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.orm.SugarRecord.findById
import dbmodel.User
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*

/**
 * Home fragment to show homescreen views and operate functionality like showing map
 */
class HomeFragment : Fragment(), OnMapReadyCallback {
    private val MY_LOCATION = "MyLocation"

    private lateinit var util: Util
    private lateinit var googleMap: GoogleMap
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocation: Location? = null

    private val familyMembers = HashMap<String, Marker>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        (activity as HomeActivity).currentFragment = FRAGMENT_HOME
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            mapView.onCreate(savedInstanceState)
            mapView.onResume()
            MapsInitializer.initialize(activity!!.applicationContext)
        } catch (ignore: Exception) {
        }

        familyMembers.clear()
        mapView.getMapAsync(this)
        util = Util(activity as Activity)
        sheetBehavior = BottomSheetBehavior.from(bottomSheet)

        reportCrimeTextView.setOnClickListener {
            shareMessageToFamily("Report a Crime!")
        }

        shareLocationTextView.setOnClickListener {
            shareMessageToFamily("Hi, I'm here!")
        }
        /*
          bottom sheet state change listener
          we are changing button text when sheet changed state
          */
        (sheetBehavior as BottomSheetBehavior<*>).setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        imageViewUpDown!!.setImageResource(R.drawable.ic_down)
                        bottomPanel!!.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        imageViewUpDown!!.setImageResource(R.drawable.ic_up)
                        bottomPanel!!.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        bottomPanel!!.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_SETTLING -> bottomPanel!!.visibility = View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        /**
         * manually opening / closing bottom sheet on button click
         */
        imageViewUpDown.setOnClickListener {
            if (sheetBehavior!!.state != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
            } else {
                sheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }

        cardViewLocation.setOnClickListener {
            getLocation()
        }
        util.showLoading(getString(R.string.please_wait))
    }

    override fun onResume() {
        try {
            mapView.onResume()
        } catch (ignore: Exception) {
        }

        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        try {
            mapView.onPause()
        } catch (ignore: Exception) {
        }

    }

    override fun onMapReady(mMap: GoogleMap) {
        try {
            googleMap = mMap
            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Objects.requireNonNull<FragmentActivity>(activity), R.raw.style_json))
            } catch (ignore: Resources.NotFoundException) {
            }

            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.uiSettings.isMapToolbarEnabled = false
            googleMap.uiSettings.isZoomGesturesEnabled = true
            googleMap.uiSettings.isRotateGesturesEnabled = true
            util.hideLoading()
            getLocation()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeLocationUpdates()
    }

    private fun getLocation() {
        if (!util.isGpsEnable) {
            displayLocationSettingsRequest(activity as Activity)
        } else if (!util.isMobileDataEnabled && !util.isWifiEnable) {
            util.toast(getString(R.string.message_turn_on_mobile_data_or_wifi))
        } else {
            Dexter.withActivity(activity)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse) {
                            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull<FragmentActivity>(activity))
                            mLocationCallback = object : LocationCallback() {
                                override fun onLocationResult(locationResult: LocationResult?) {
                                    super.onLocationResult(locationResult)
                                    onNewLocation(locationResult!!.lastLocation)
                                }
                            }
                            createLocationRequest()
                            getLastLocation()
                            requestLocationUpdates()
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse) {
                            util.showSettingsDialog(activity as Activity)
                        }

                        override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                            token.continuePermissionRequest()
                        }
                    }).check()
        }
    }

    fun createLocationRequest() {
        try {
            val FASTEST_UPDATE_INTERVAL_IN_SECONDS = UPDATE_INTERVAL_IN_SECONDS / 2 * 1000
            UPDATE_INTERVAL_IN_SECONDS *= 1000
            mLocationRequest = LocationRequest()
            mLocationRequest!!.interval = UPDATE_INTERVAL_IN_SECONDS
            mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_SECONDS
            mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        } catch (ignore: Exception) {
        }

    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient!!.lastLocation
                    .addOnFailureListener { util.log("onFailure", "onFailure") }
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            mLocation = task.result
                            mLocation?.let {
                                onNewLocation(it)
                            }
                        }
                    }
        } catch (ignore: SecurityException) {
        }

    }

    fun removeLocationUpdates() {
        if (mFusedLocationClient != null) {
            try {
                mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
            } catch (unlikely: SecurityException) {
                unlikely.printStackTrace()
            }

        }
    }

    fun requestLocationUpdates() {
        try {
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest,
                    mLocationCallback!!, Looper.myLooper())
        } catch (unlikely: SecurityException) {
            unlikely.printStackTrace()
        }

    }

    private fun onNewLocation(location: Location) {
        try {
            mLocation = location
            if (mLocation != null) {
                val homeActivity = (Objects.requireNonNull<FragmentActivity>(activity) as HomeActivity)
                homeActivity.setCity(util.getCity(mLocation!!.latitude, mLocation!!.longitude))
                findById(User::class.java, 1)?.apply {
                    moveCamera(LatLng(mLocation!!.latitude, mLocation!!.longitude))
                    val userImpl = UserImpl(homeActivity)
                    userImpl.updateUserGeoLocation(phone, GeoPoint(mLocation!!.latitude, mLocation!!.longitude)) { success ->
                        if(success) {
                            userImpl.getFamilyMembers(familyCode) { members ->
                                members.mapNotNull {
                                    val lat = it.geoLocation?.latitude
                                    val lon = it.geoLocation?.longitude
                                    if(lat != null && lon != null)
                                        FamilyMemberData("${it.firstName} ${it.lastName}", it.uriPath!!, LatLng(lat, lon), it.sharedMessage)
                                    else null
                                }.forEach loop@{
                                    if(homeActivity.isDestroyed) return@loop

                                    val display = homeActivity.windowManager.defaultDisplay
                                    val size = Point()
                                    display.getSize(size)
                                    val circleSize = Math.round(size.x * 0.15f)

                                    val borderSize = circleSize * 0.1f
                                    val borderColor = if(it.avatarUrl == this.uriPath) RED else GREEN

                                    Glide.with(homeActivity)
                                            .asBitmap()
                                            .load(it.avatarUrl)
                                            .apply(RequestOptions().circleCrop().override(circleSize, circleSize))
                                            .into(object : CustomTarget<Bitmap>() {
                                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                    val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(
                                                            homeActivity.resources,
                                                            if (borderSize > 0) {
                                                                resource.addBorder(borderSize, borderColor)
                                                            } else {
                                                                resource
                                                            }
                                                    )
                                                    circularBitmapDrawable.isCircular = true
                                                    val markerIcon = BitmapDescriptorFactory.fromBitmap(circularBitmapDrawable.bitmap)
                                                    val markerOptions = MarkerOptions().position(it.latLng!!).icon(markerIcon)
                                                    markerOptions.title(it.sharedMessage)
                                                    updateMarkerLocation(it.avatarUrl, it.latLng, markerOptions)
                                                }

                                                override fun onLoadCleared(placeholder: Drawable?) {
                                                    // this is called when imageView is cleared on lifecycle call or for
                                                    // some other reason.
                                                    // if you are referencing the bitmap somewhere else too other than this imageView
                                                    // clear it here as you can no longer have the bitmap
                                                }
                                            })
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun shareMessageToFamily(message: String) {
        findById(User::class.java, 1)?.apply {
            if(activity != null) UserImpl(activity!!).updateUserField(phone, SHARED_MESSAGE, message)
        }
    }

    private fun updateMarkerLocation(markerKey: String, latLng: LatLng, newMarker: MarkerOptions) {
        if(familyMembers[markerKey] == null) {
            familyMembers[markerKey] = googleMap.addMarker(newMarker)
        } else {
            familyMembers[markerKey]?.apply {
                position = latLng
                title = newMarker.title
                if(!TextUtils.isEmpty(title)) {
                    moveCamera(latLng)
                    showInfoWindow()
                } else hideInfoWindow()
            }
        }
    }
    

    private fun moveCamera(latLng: LatLng?) {
        if (latLng != null) {
            val cameraPosition = CameraPosition.Builder().target(latLng).zoom(12f).build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    companion object {
        private var UPDATE_INTERVAL_IN_SECONDS = DEFAULT_LOCATION_UPDATE_TIME_INTERVAL.toLong()
    }
}
