package com.example.avocode.fragment;


import android.Manifest;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.avocode.R;
import com.example.avocode.activity.HomeActivity;
import com.example.avocode.utils.Util;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.example.avocode.config.Constants.DEFAULT_LOCATION_UPDATE_TIME_INTERVAL;
import static com.example.avocode.config.Constants.FRAGMENT_HOME;
import static com.example.avocode.utils.Util.displayLocationSettingsRequest;

/**
 * Home fragment to show homescreen views and operate functionality like showing map
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private Util util;
    private GoogleMap googleMap;
    private MapView mapView;
    @BindView(R.id.bottomSheet)
    RelativeLayout layoutBottomSheet;
    @BindView(R.id.imageViewUpDown)
    ImageView imageViewUpDown;
    @BindView(R.id.linearBottom)
    LinearLayout linearBottom;
    private Unbinder unbinder;
    private BottomSheetBehavior sheetBehavior;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mLocation;
    private static long UPDATE_INTERVAL_IN_SECONDS = DEFAULT_LOCATION_UPDATE_TIME_INTERVAL;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        ((HomeActivity) Objects.requireNonNull(getActivity())).currentFragment = FRAGMENT_HOME;
        mapView = view.findViewById(R.id.mapView);
        try {
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception ignore) {
        }
        mapView.getMapAsync(this);
        util = new Util(getActivity());
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        /*
          bottom sheet state change listener
          we are changing button text when sheet changed state
          */
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        imageViewUpDown.setImageResource(R.drawable.ic_down);
                        linearBottom.setVisibility(View.VISIBLE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        imageViewUpDown.setImageResource(R.drawable.ic_up);
                        linearBottom.setVisibility(View.GONE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING: {
                        linearBottom.setVisibility(View.VISIBLE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        linearBottom.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        util.showLoading(getString(R.string.please_wait));
        return view;
    }

    @Override
    public void onResume() {
        try {
            mapView.onResume();
        } catch (Exception ignore) {
        }
        super.onResume();
    }

    /**
     * manually opening / closing bottom sheet on button click
     */
    @OnClick(R.id.imageViewUpDown)
    public void toggleBottomSheet() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @OnClick(R.id.cardViewLocation)
    public void getLocationClicked() {
        if (!util.isGpsEnable()) {
            displayLocationSettingsRequest(getActivity());
        } else if (!util.isMobileDataEnabled() && !util.isWifiEnable()) {
            util.toast(getString(R.string.message_turn_on_mobile_data_or_wifi));
        } else {
            getLocation();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        try {
            mapView.onPause();
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        try {
            googleMap = mMap;
            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(Objects.requireNonNull(getActivity()), R.raw.style_json));
            } catch (Resources.NotFoundException ignore) {
            }
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(true);
            googleMap.getUiSettings().setRotateGesturesEnabled(true);
            util.hideLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        removeLocationUpdates();
    }

    private void getLocation() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
                        mLocationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                onNewLocation(locationResult.getLastLocation());
                            }
                        };
                        createLocationRequest();
                        getLastLocation();
                        requestLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        util.showSettingsDialog(getActivity());
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public void createLocationRequest() {
        try {
            long FASTEST_UPDATE_INTERVAL_IN_SECONDS = ((UPDATE_INTERVAL_IN_SECONDS / 2) * 1000);
            UPDATE_INTERVAL_IN_SECONDS = (UPDATE_INTERVAL_IN_SECONDS * 1000);
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_SECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_SECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        } catch (Exception ignore) {
        }
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                                onNewLocation(mLocation);
                            }
                        }
                    });
        } catch (SecurityException ignore) {
        }
    }

    public void removeLocationUpdates() {
        if (mFusedLocationClient != null) {
            try {
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            } catch (SecurityException unlikely) {
                unlikely.printStackTrace();
            }
        }
    }

    public void requestLocationUpdates() {
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            unlikely.printStackTrace();
        }
    }

    private void onNewLocation(Location location) {
        try {
            mLocation = location;
            if (googleMap != null & mLocation != null) {
                ((HomeActivity) Objects.requireNonNull(getActivity())).setCity(util.getCity(mLocation.getLatitude(), mLocation.getLongitude()));
                googleMap.addMarker(new MarkerOptions().position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())).title("It's Me!"));
                moveCamera(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveCamera(LatLng latLng) {
        if (latLng != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }


}
