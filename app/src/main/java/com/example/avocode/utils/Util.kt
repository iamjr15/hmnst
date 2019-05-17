package com.example.avocode.utils

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.avocode.R
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.orm.SugarRecord.findById
import dbmodel.User
import java.util.*

// An Utility class to perform basic operations of an app like showing/hiding dialogs,toast, checking values and status etc.
class Util(private val context: Context) {
    private var progressDialog: ProgressDialog? = null

    val isMobileDataEnabled: Boolean
        get() {
            val connectivityService = context.getSystemService(CONNECTIVITY_SERVICE)
            val cm = connectivityService as ConnectivityManager

            try {
                val c = Class.forName(Objects.requireNonNull(cm).javaClass.name)
                val m = c.getDeclaredMethod("getMobileDataEnabled")
                m.isAccessible = true
                return m.invoke(cm) as Boolean
            } catch (e: Exception) {
                return false
            }

        }

    val isWifiEnable: Boolean
        get() {
            return try {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                Objects.requireNonNull(wifiManager).isWifiEnabled
            } catch (e: Exception) {
                false
            }

        }

    val isGpsEnable: Boolean
        get() {
            try {
                val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                return Objects.requireNonNull(manager).isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (e: Exception) {
                return false
            }

        }

    //Check whether user is logged in or not
    val isUseerLoggedIn: Boolean
        get() = findById(User::class.java, 1) != null

    fun showLoading(msg: String) {
        try {
            progressDialog = ProgressDialog.show(context, "", msg, true)
        } catch (ignore: Exception) {
        }

    }

    fun hideLoading() {
        try {
            if (progressDialog != null && progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        } catch (ignore: Exception) {
        }

    }

    fun toast(message: String?) {
        Toast.makeText(context, message ?: "", Toast.LENGTH_LONG).show()
    }

    fun log(title: String, msg: String) {
        Log.println(Log.ASSERT, "" + title, "" + msg)
    }


    fun showSettingsDialog(activity: Activity) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.need_permission)
        builder.setMessage(R.string.message_permission_settings)
        builder.setPositiveButton(R.string.go_to_settings) { dialog, which ->
            dialog.cancel()
            openSettings(activity)
        }
        builder.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }
        builder.show()

    }

    private fun openSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivityForResult(intent, 101)
    }

    fun saveUser(firstName: String, lastName: String, gender: String, dob: String, phone: String, uriPath: String, familyCode: String) {
        val user = findById(User::class.java, 1)
        if (user != null) {
            user.firstName = firstName
            user.lastName = lastName
            user.gender = gender
            user.dob = dob
            user.phone = phone
            user.uriPath = uriPath
            user.familyCode = familyCode
            user.save()
        } else {
            val user1 = User(firstName, lastName, gender, dob, phone, uriPath, familyCode)
            user1.save()
        }
    }


    fun getCity(latitude: Double, longitude: Double): String {
        val strAdd = "Not available"
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            val address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            val city = addresses[0].locality
            val state = addresses[0].adminArea
            val country = addresses[0].countryName
            val postalCode = addresses[0].postalCode
            val knownName = addresses[0].featureName // Only if available else return NULL
            return city
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("Address", "Cannot get Address!")
        }

        return strAdd
    }

    companion object {

        private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        fun generateFamilyCode(): String {
            return (1..5)
                    .map { kotlin.random.Random.nextInt(0, charPool.size) }
                    .map(charPool::get)
                    .joinToString("")
        }

        fun checkEmptyStrings(string: String?): Boolean {
            return TextUtils.isEmpty(string)
        }

        //Display system dialog when GPS is off
        fun displayLocationSettingsRequest(context: Activity) {
            val REQUEST_CHECK_SETTINGS = 0x1
            val googleApiClient = GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API).build()
            googleApiClient.connect()

            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 10000
            locationRequest.fastestInterval = (10000 / 2).toLong()

            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)

            val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
            result.setResultCallback { result1 ->
                val status = result1.status
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> Log.i("LocationDialog", "All location settings are satisfied.")
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.i("LocationDialog", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ")

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(context, REQUEST_CHECK_SETTINGS)
                        } catch (e: IntentSender.SendIntentException) {
                            Log.i("LocationDialog", "PendingIntent unable to execute request.")
                        }

                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.i("LocationDialog", "Location settings are inadequate, and cannot be fixed here. Dialog not created.")
                }
            }
        }

        fun showFamilyCode(tv: TextView, newFamilyCode: String) {
             tv.text = String.format(tv.context.getString(R.string.family_code_with_value), newFamilyCode)
        }
    }
}
