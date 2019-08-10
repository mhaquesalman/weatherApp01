package com.salman.weatherforecaster;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.salman.weatherforecaster.adapter.PageAdapter;
import com.salman.weatherforecaster.common.Common;
import com.salman.weatherforecaster.fragment.CityFragment;
import com.salman.weatherforecaster.fragment.CurrentWeatherFragment;
import com.salman.weatherforecaster.fragment.ForcastWeatherFragment;
import com.salman.weatherforecaster.fragment.PlacesFragment;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private TabItem tabCurrent;
    private TabItem tabForecast;
    private ViewPager viewPager;
    CardView cardView;
    private PageAdapter pageAdapter;
    ProgressDialog progressDialog;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    public static Location location;
    public static double lat;
    public static double lon;

    private static final String API_KEY = "AIzaSyAfjybPkW3yYWg3EtS6jUS0yTWEwzeBX5I";
    PlacesClient placesClient;
    public static final String SETTINGS = "settings_typesOfWeather";
    private boolean isSettingsChanged = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = findViewById(R.id.tablayout);
        tabCurrent = findViewById(R.id.tabCurrent);
        tabForecast = findViewById(R.id.tabForcast);
        viewPager = findViewById(R.id.viewPager);

        PreferenceManager.setDefaultValues(MainActivity.this, R.xml.my_preferences, false);
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                .registerOnSharedPreferenceChangeListener(settingsChangeListener);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        // pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        // viewPager.setAdapter(pageAdapter);

     /*   viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        }); */

     /*   if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }
        placesClient = Places.createClient(this);
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng latLng = place.getLatLng();
                Log.d(TAG, "Location: "+ "lat: " +latLng.latitude + "lng: " +latLng.longitude);
            }
            @Override
            public void onError(@NonNull Status status) {
                Log.e(TAG, "onError: "+status.getStatusMessage());
            }
        });  */

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            buildLocationRequest();
                            buildLocationCallback();

                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }).check();

        progressDialog.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent preferencesIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    public SharedPreferences.OnSharedPreferenceChangeListener settingsChangeListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            isSettingsChanged = true;
            String selectedUnits= sharedPreferences.getString(MainActivity.SETTINGS, null);
            Log.e(TAG, "onSharedPreferenceChanged: "+ selectedUnits);
            if (key.equals(SETTINGS)) {
                CurrentWeatherFragment.getInstance().changeUnits(selectedUnits);
            }
            Toast.makeText(MainActivity.this, "Change applied", Toast.LENGTH_SHORT).show();
        }
    };

    public void openPlaces() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), API_KEY);
        }
        placesClient = Places.createClient(this);
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng latLng = place.getLatLng();
                Log.d(TAG, "onPlaceSelected: " + "lat: " + latLng.latitude + "lng: " + latLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e(TAG, "onError: " + status.getStatusMessage());
            }
        });
    }

    public void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //DecimalFormat df = new DecimalFormat("#.00");
                location = locationResult.getLastLocation();
                lat = Double.parseDouble(String.format("%.2f", location.getLatitude()));
                lon = Double.parseDouble(String.format("%.2f", location.getLongitude()));

                Common.current_location = locationResult.getLastLocation();
                setupViewPager(viewPager);
                tabLayout.setupWithViewPager(viewPager);

                //lat = location.getLatitude();
                //lon = location.getLongitude();

                /* Bundle bundle = new Bundle();
                bundle.putString("lat", lat);
                bundle.putString("lon", lon);
                // set Fragmentclass Arguments
                CurrentWeatherFragment fragment = new CurrentWeatherFragment();
                fragment.setArguments(bundle); */

                Log.d(TAG, "Location: " + lat + " / " + lon);
            }
        };
    }

    public void setupViewPager(ViewPager viewPager) {
        PageAdapter adapter = new PageAdapter(getSupportFragmentManager());
        adapter.addFragment(CurrentWeatherFragment.getInstance(), "Current Weather");
        adapter.addFragment(ForcastWeatherFragment.getInstance(), "5 Days Weather");
        adapter.addFragment(CityFragment.getInstance(), "City");
        progressDialog.dismiss();
        viewPager.setAdapter(adapter);
    }

    public void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10.0f);
    }

}


