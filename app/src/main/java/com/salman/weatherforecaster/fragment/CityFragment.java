package com.salman.weatherforecaster.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.label305.asynctask.SimpleAsyncTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.salman.weatherforecaster.R;
import com.salman.weatherforecaster.common.Common;
import com.salman.weatherforecaster.model.WeatherResult;
import com.salman.weatherforecaster.retrofit.RetrofitClient;
import com.salman.weatherforecaster.retrofit.WeatherService;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class CityFragment extends Fragment {
    private static final String TAG = "CityFragment";
    List<String> listCities;
    MaterialSearchBar searchBar;
    ImageView imageView;
    TextView temparature, date, day, city, min, max, sunrise, sunset, humidity, pressure, main;
    CompositeDisposable compositeDisposable;
    WeatherService weatherService;
    String cityName;

    static CityFragment instance;

    public static CityFragment getInstance() {
        if (instance == null) {
            instance = new CityFragment();
        }
        return instance;
    }

    public CityFragment() {
        compositeDisposable = new CompositeDisposable();
        weatherService = RetrofitClient.getRetrofit().create(WeatherService.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_city, container, false);
        imageView = itemView.findViewById(R.id.image);
        temparature = itemView.findViewById(R.id.temparature);
        date = itemView.findViewById(R.id.date);
        day = itemView.findViewById(R.id.day);
        city = itemView.findViewById(R.id.city);
        min = itemView.findViewById(R.id.min);
        max = itemView.findViewById(R.id.max);
        sunrise = itemView.findViewById(R.id.sunrise);
        sunset = itemView.findViewById(R.id.sunset);
        humidity = itemView.findViewById(R.id.humidity);
        pressure = itemView.findViewById(R.id.pressure);
        main = itemView.findViewById(R.id.main);

        searchBar = itemView.findViewById(R.id.searchBar);
        searchBar.setEnabled(false);

        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        cityName = preferences.getString("city", "Dhaka District,BD");

        getWeatherInformation(cityName);
        //AsyncTask class to load cities list
        new LoadCities().execute();
        return itemView;
    }

    private class LoadCities extends SimpleAsyncTask<List<String>> {
        @Override
        protected List<String> doInBackgroundSimple() {
            listCities = new ArrayList<>();
            try {
                StringBuilder builder = new StringBuilder();
                InputStream inputStream = getResources().openRawResource(R.raw.city_list);
                GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
                InputStreamReader reader = new InputStreamReader(gzipInputStream);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String readed;
                while ((readed = bufferedReader.readLine()) != null) {
                    builder.append(readed);
                }
                listCities = new Gson().fromJson(builder.toString(), new TypeToken<List<String>>() {
                }.getType());

            } catch (IOException e) {
                e.printStackTrace();
            }
            return listCities;
        }

        @Override
        protected void onSuccess(final List<String> listCity) {
            super.onSuccess(listCity);
            searchBar.setEnabled(true);
            searchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    List<String> suggest = new ArrayList<>();
                    for (String search : listCity) {
                        if (search.toLowerCase().contains(searchBar.getText().toLowerCase())) {
                            suggest.add(search);
                        }
                    }
                    searchBar.setLastSuggestions(suggest);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {

                }

                @Override
                public void onSearchConfirmed(CharSequence text) {
                    getWeatherInformation(text.toString());
                    searchBar.setLastSuggestions(listCity);
                    SharedPreferences.Editor editor = getContext().getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                    editor.putString("city", text.toString());
                    editor.apply();

                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });
            searchBar.setLastSuggestions(listCity);
        }
    }

    public void getWeatherInformation(String cityName) {
        compositeDisposable.add(weatherService.getWeatherByCity(cityName, Common.APP_ID, "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {
                        Log.d(TAG, "accept: " + weatherResult.toString());
                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                                .append(weatherResult.getWeather().get(0).getIcon())
                                .append(".png").toString()).into(imageView);
                        temparature.setText(new StringBuilder(
                                String.valueOf(weatherResult.getMain().getTemp()))
                                .append("°С").toString());
                        city.setText(weatherResult.getName());
                        Log.d(TAG, "onResponse: " + weatherResult.getName());
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
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                })
        );
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}
