package com.salman.weatherforecaster.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.salman.weatherforecaster.R;
import com.salman.weatherforecaster.common.Common;
import com.salman.weatherforecaster.model.WeatherResult;
import com.salman.weatherforecaster.retrofit.RetrofitClient;
import com.salman.weatherforecaster.retrofit.WeatherService;
import com.squareup.picasso.Picasso;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class CurrentWeatherFragment extends Fragment {
    private static final String TAG = "CurrentWeatherFragment";
    Context mContex;
    ImageView imageView;
    TextView temparature, date, day, city, min, max, sunrise, sunset, humidity, pressure, main;
    CompositeDisposable compositeDisposable;
    WeatherService weatherService;
    public static String units = "";
    public static String selectUnit = "";

    static CurrentWeatherFragment instance;
    public static CurrentWeatherFragment getInstance() {
        if (instance == null) {
            instance = new CurrentWeatherFragment();
        }
        return instance;
    }

    public CurrentWeatherFragment() {
         compositeDisposable = new CompositeDisposable();
         Retrofit retrofit = RetrofitClient.getRetrofit();
         weatherService = retrofit.create(WeatherService.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        //weatherService = RetrofitClient.getRetrofit().create(WeatherService.class);
        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        units = preferences.getString("units", "metric");
        selectUnit = preferences.getString("selectUnit", "°С");

        Log.d(TAG, "onCreateView: " + units + selectUnit + Common.current_location.toString());
        getWeatherInformation();
        return itemView;
    }

    //change units from prefrences
    public void changeUnits(String selectedUnits) {
        if (selectedUnits.equals("°С")) {
            units = "metric";
            selectUnit = selectedUnits;
        } else if (selectedUnits.equals("°F")) {
            units = "imperial";
            selectUnit = selectedUnits;
        }
        SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("units", units);
        editor.putString("selectUnit", selectUnit);
        editor.apply();
    }

    // 23.74, 90.37
    public void getWeatherInformation() {
        //progressDialog.show();
        String urlString = String.format("weather?lat=%s&lon=%s&appid=%s&units=%s",String.valueOf(Common.current_location.getLatitude()),String.valueOf(Common.current_location.getLongitude()),Common.APP_ID,units);
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
                            .append(selectUnit).toString());
                    city.setText(weatherResult.getName());
                    Log.d(TAG, "onResponse: "+weatherResult.getName());
                    date.setText(Common.convertUnixToDate(weatherResult.getDt()));
                    day.setText(Common.convertUnixToDay(weatherResult.getDt()));
                    pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                    humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());
                    sunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                    sunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                    min.setText(weatherResult.getMain().getTemp_min() + " " + selectUnit);
                    max.setText(weatherResult.getMain().getTemp_max() + " " + selectUnit);
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

