package com.salman.weatherforecaster.fragment;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.salman.weatherforecaster.MainActivity;
import com.salman.weatherforecaster.R;
import com.salman.weatherforecaster.common.Common;
import com.salman.weatherforecaster.model.WeatherResult;
import com.salman.weatherforecaster.retrofit.RetrofitClient;
import com.salman.weatherforecaster.retrofit.WeatherService;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.support.constraint.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class CurrentWeatherFragment extends Fragment {
    private static final String TAG = "CurrentWeatherFragment";
    Context mContex;
    ImageView imageView;
    TextView temparature, date, day, city, min, max, sunrise, sunset, humidity, pressure, main;
    ProgressBar loading;
    CompositeDisposable compositeDisposable;
    WeatherService weatherService;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    public static Location location;
    public static double lat;
    public static double lon;
    public static String units = "metric";


    public CurrentWeatherFragment() {
        compositeDisposable = new CompositeDisposable();
      //  Retrofit retrofit = RetrofitClient.getRetrofit();
      //  weatherService = retrofit.create(WeatherService.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_current_weather, container, false);
        imageView = itemView.findViewById(R.id.image);
        temparature = itemView.findViewById(R.id.temparature);
        date = itemView.findViewById(R.id.date);
        day =  itemView.findViewById(R.id.day);
        city = itemView.findViewById(R.id.city);
        min = itemView.findViewById(R.id.min);
        max = itemView.findViewById(R.id.max);
        sunrise = itemView.findViewById(R.id.sunrise);
        sunset = itemView.findViewById(R.id.sunset);
        humidity = itemView.findViewById(R.id.humidity);
        pressure = itemView.findViewById(R.id.pressure);
        main = itemView.findViewById(R.id.main);

        weatherService = RetrofitClient.getRetrofit().create(WeatherService.class);

        Dexter.withActivity(getActivity())
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            buildLocationRequest();
                            buildLocationCallback();

                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }).check();

        return itemView;
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
                getWeatherInformation();

                //lat = location.getLatitude();
                //lon = location.getLongitude();

/*              Bundle bundle = new Bundle();
                bundle.putString("lat", lat);
                bundle.putString("lon", lon);
                // set Fragmentclass Arguments
                CurrentWeatherFragment fragment = new CurrentWeatherFragment();
                fragment.setArguments(bundle);*/

                //Common.current_location = locationResult.getLastLocation();
                //CurrentWeatherFragment.location = locationResult.getLastLocation();
                //Log.d(TAG, "Common: " + Common.current_location);
                //Log.d(TAG, "CurrentWeatherFragment: " + CurrentWeatherFragment.location);
                //Log.d(TAG, "onLocationResult: " + Common.current_location.getLatitude() + " / " + Common.current_location.getLongitude());
                Log.d(TAG, "Location: " + lat + " / " + lon);
            }
        };
    }

    public void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10.0f);
    }

    public void getWeatherInformation() {
        String urlString = String.format("weather?lat=%s&lon=%s&appid=%s&units=%s",String.valueOf(lat),String.valueOf(lon),Common.APP_ID,units);
        Call<WeatherResult> call = weatherService.getCurrentWeatherResponse(urlString);
        call.enqueue(new Callback<WeatherResult>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResult> call, @NonNull Response<WeatherResult> response) {
                Log.d(TAG, "1.onResponse: " +response.code());
                Log.d(TAG, "2.onResponse: "+response.body());
                if (response.code() == 200) {
                    WeatherResult weatherResult = response.body();
                    // Load image
                    Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                            .append(weatherResult.getWeather().get(0).getIcon())
                            .append(".png").toString()).into(imageView);
                    temparature.setText(new StringBuilder(
                            String.valueOf(weatherResult.getMain().getTemp()))
                            .append("°С").toString());
                    city.setText(weatherResult.getName());
                    Log.d(TAG, "onResponse: "+weatherResult.getName());
                    date.setText(Common.convertUnixToDate(weatherResult.getDt()));
                    day.setText(Common.convertUnixToDay(weatherResult.getDt()));
                    pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                    humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());
                    sunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                    sunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                    min.setText(weatherResult.getMain().getTemp_min() + " °С");
                    max.setText(weatherResult.getMain().getTemp_max() + " °С");
                    main.setText(weatherResult.getWeather().get(0).getMain());
                }
            }
            @Override
            public void onFailure(Call<WeatherResult> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage() );
            }
        });

    }

/*    @Override
    public void onAttach(Context context) {
        this.mContex = context;
        super.onAttach(context);
    }*/
}
