package com.example.avocode.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avocode.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import dbmodel.User;

import static android.content.Context.CONNECTIVITY_SERVICE;

// An Utility class to perform basic operations of an app like showing/hiding dialogs,toast, checking values and status etc.
public class Util {
    private ProgressDialog progressDialog;
    private Context context;
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    public Util(Context context) {
        this.context = context;
    }

    public void showLoading(String msg) {
        try {
            if (progressDialog == null) {
                progressDialog = ProgressDialog.show(context, "", msg, true);
            }
        } catch (Exception ignore) {
        }
    }

    public void hideLoading() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception ignore) {
        }
    }

    public void toast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public void log(String title, String msg) {
        Log.println(Log.ASSERT, "" + title, "" + msg);
    }

    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (i < 2) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
        }
        return false;
    }

    public static boolean checkEmptyStrings(String string) {
        return TextUtils.isEmpty(string);
    }

    public void showSnackBar(Activity activity, String message) {
        View view = activity.findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public boolean isMobileDataEnabled() {
        Object connectivityService = context.getSystemService(CONNECTIVITY_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) connectivityService;

        try {
            Class<?> c = Class.forName(Objects.requireNonNull(cm).getClass().getName());
            Method m = c.getDeclaredMethod("getMobileDataEnabled");
            m.setAccessible(true);
            return (Boolean) m.invoke(cm);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isWifiEnable() {
        try {
            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return Objects.requireNonNull(wifiManager).isWifiEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isGpsEnable() {
        try {
            LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return Objects.requireNonNull(manager).isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    public double roundTwoDecimals(double d) {
        try {
            DecimalFormat twoDForm = new DecimalFormat("#.##");
            return Double.valueOf(twoDForm.format(d));
        } catch (Exception e) {
            return d;
        }
    }


    public void showSettingsDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.need_permission);
        builder.setMessage(R.string.message_permission_settings);
        builder.setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings(activity);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, 101);
    }

    public void saveUser(String firstName, String lastName, String gender, String dob, String phone, String uriPath) {
        User user = User.findById(User.class, 1);
        if (user != null) {
            user.firstName = firstName;
            user.lastName = lastName;
            user.gender = gender;
            user.dob = dob;
            user.phone = phone;
            user.uriPath = uriPath;
            user.save();
        } else {
            User user1 = new User(firstName, lastName, gender, dob, phone, uriPath);
            user1.save();
        }
    }

    //Check whether user is logged in or not
    public boolean isUseerLoggedIn() {
        return User.findById(User.class, 1) != null;
    }

    //Display system dialog when GPS is off
    public static void displayLocationSettingsRequest(final Activity context) {
        final int REQUEST_CHECK_SETTINGS = 0x1;
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result1) {
                final Status status = result1.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("LocationDialog", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("LocationDialog", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(context, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("LocationDialog", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("LocationDialog", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }


    public String getCity(double latitude, double longitude) {
        String strAdd = "Not available";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            return city;
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Address", "Cannot get Address!");
        }
        return strAdd;
    }
}
